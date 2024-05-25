package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.exceptions.OutputSourceMissingException;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_ID;
import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class TaskReplayFactory {

  private final Map<String, MeshineryTask<?, ? extends MeshineryDataContext>> taskMap;
  private final ExecutorService executorService;

  /**
   * Creates a new TaskReplayFactory.
   *
   * @param tasks           list of tasks to choose from
   * @param executorService the executorService which should be used
   */
  public TaskReplayFactory(
      List<MeshineryTask<?, ? extends MeshineryDataContext>> tasks, ExecutorService executorService
  ) {
    this.executorService = executorService;
    this.taskMap = tasks.stream()
        .collect(Collectors.toMap(MeshineryTask::getTaskName, Function.identity()));
  }

  /**
   * Injects a new data context into a task, by name, synchronous.
   *
   * @param taskName to use
   * @param context  to use
   * @param <C>      type of the context
   * @throws ExecutionException   throws execution exception
   * @throws InterruptedException throws interrupted exception
   */
  public <C extends MeshineryDataContext> MeshineryDataContext injectData(String taskName, C context)
      throws ExecutionException, InterruptedException, MeshineryTaskNotFoundException {

    var result = createTaskInjection(taskName, context).get();

    MDC.clear();

    return result;
  }

  /**
   * Injects a new data context into a task, by name, asynchronous.
   *
   * @param taskName to use
   * @param context  to use
   * @param <C>      type of the context
   */
  public <C extends MeshineryDataContext> CompletableFuture<C> injectDataAsync(String taskName, C context)
      throws MeshineryTaskNotFoundException {

    MDC.put(TASK_NAME, taskName);
    MDC.put(TASK_ID, context.getId());
    log.info("Replaying a new Context asynchronous");

    var result = createTaskInjection(taskName, context);

    MDC.clear();

    return result;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public <C extends MeshineryDataContext> void replayData(String taskName, C context)
      throws MeshineryTaskNotFoundException {
    var replacedTaskName = taskName.replace('_', ' ');

    MDC.put(TASK_NAME, replacedTaskName);
    MDC.put(TASK_ID, context.getId());
    log.info("Replaying a new context");

    MeshineryTask<Object, C> task = getMeshineryTask(replacedTaskName);

    var inputKeys = task.getInputKeys();

    if (!MeshinerySourceConnector.class.isAssignableFrom(task.getInputConnector().getClass())) {
      throw new OutputSourceMissingException("Cant replay task since the output source of the input source is unknown");
    }

    var inputConnector = (MeshinerySourceConnector) task.getInputConnector();
    inputConnector.writeOutput(inputKeys.get(0), context, new TaskData());

    MDC.clear();
  }

  private <C extends MeshineryDataContext> MeshineryTask<Object, C> getMeshineryTask(String replacedTaskName)
      throws MeshineryTaskNotFoundException {
    if (!taskMap.containsKey(replacedTaskName)) {
      throw new MeshineryTaskNotFoundException("Could not find Task with name '%s' in [%s]"
          .formatted(replacedTaskName, String.join(", ", taskMap.keySet())));
    }

    return (MeshineryTask<Object, C>) taskMap.get(replacedTaskName);
  }

  private <C extends MeshineryDataContext> CompletableFuture<C> createTaskInjection(String taskName, C context)
      throws MeshineryTaskNotFoundException {

    var replacedTaskName = taskName.replace('_', ' ');

    MDC.put(TASK_NAME, replacedTaskName);
    MDC.put(TASK_ID, context.getId());

    log.info("Injecting a new Context synchronous");

    if (!taskMap.containsKey(replacedTaskName)) {
      throw new MeshineryTaskNotFoundException("Could not find Task with name '%s' in [%s]"
          .formatted(replacedTaskName, String.join(", ", taskMap.keySet())));
    }

    var task = taskMap.get(replacedTaskName);

    return MeshineryUtils.combineProcessors(
        task.getProcessorList(),
        context,
        executorService,
        MDC.getCopyOfContextMap(),
        task.getTaskData()
    );
  }

}
