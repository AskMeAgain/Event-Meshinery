package ask.me.again.meshinery.core.task;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import static ask.me.again.meshinery.core.task.TaskDataProperties.TASK_NAME;
import static ask.me.again.meshinery.core.task.TaskDataProperties.UID;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
public class TaskReplayFactory {

  private final Map<String, MeshineryTask<?, ? extends Context>> taskMap;
  private final ExecutorService executorService;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public TaskReplayFactory(List<MeshineryTask<?, ? extends Context>> tasks, ExecutorService executorService) {
    this.executorService = executorService;
    this.taskMap = tasks.stream()
        .collect(Collectors.toMap(MeshineryTask::getTaskName, Function.identity()));
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public <C extends Context> void replay(String taskName, C context) throws ExecutionException, InterruptedException {

    MDC.put(TASK_NAME, taskName);
    MDC.put(UID, context.getId());
    log.info("Replaying a new Context");

    var task = taskMap.get(taskName);

    MeshineryUtils.combineProcessors(
        task.getProcessorList(),
        context,
        executorService,
        MDC.getCopyOfContextMap(),
        task.getTaskData()
    ).get();

    MDC.clear();
  }

}
