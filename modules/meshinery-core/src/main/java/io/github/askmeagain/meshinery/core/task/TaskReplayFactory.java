package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.DataContext;
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

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;
import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.UID;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class TaskReplayFactory {

  private final Map<String, MeshineryTask<?, ? extends DataContext>> taskMap;
  private final ExecutorService executorService;

  /**
   * Creates a new TaskReplayFactory.
   *
   * @param tasks           list of tasks to choose from
   * @param executorService the executorService which should be used
   *
   */
  public TaskReplayFactory(List<MeshineryTask<?, ? extends DataContext>> tasks, ExecutorService executorService) {
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
  public <C extends DataContext> void injectData(String taskName, C context)
      throws ExecutionException, InterruptedException {

    createTaskInjection(taskName, context).get();

    MDC.clear();
  }

  /**
   * Injects a new data context into a task, by name, asynchronous.
   *
   * @param taskName to use
   * @param context  to use
   * @param <C>      type of the context
   */
  public <C extends DataContext> CompletableFuture<C> injectDataAsync(String taskName, C context) {

    MDC.put(TASK_NAME, taskName);
    MDC.put(UID, context.getId());
    log.info("Replaying a new Context asynchronous");

    var result = createTaskInjection(taskName, context);

    MDC.clear();

    return result;
  }

  private <C extends DataContext> CompletableFuture<C> createTaskInjection(String taskName, C context) {

    MDC.put(TASK_NAME, taskName);
    MDC.put(UID, context.getId());
    log.info("Replaying a new Context synchronous");

    var task = taskMap.get(taskName);

    return MeshineryUtils.combineProcessors(
        task.getProcessorList(),
        context,
        executorService,
        MDC.getCopyOfContextMap(),
        task.getTaskData()
    );
  }

}
