package ask.me.again.meshinery.core.task;

import ask.me.again.meshinery.core.common.ComposableFutureUtils;
import ask.me.again.meshinery.core.common.Context;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class TaskReplayFactory {

  private final Map<String, MeshineryTask<?, ? extends Context>> taskMap;
  private final ExecutorService executorService;

  public TaskReplayFactory(List<MeshineryTask<?, ? extends Context>> tasks, ExecutorService executorService) {
    this.executorService = executorService;
    this.taskMap = tasks.stream()
        .collect(Collectors.toMap(MeshineryTask::getTaskName, Function.identity()));
  }

  public <C extends Context> void replay(String taskName, C context) throws ExecutionException, InterruptedException {

    MDC.put("task.name", taskName);
    MDC.put("uid", context.getId());
    log.info("Replaying a new Context");

    var meshineryTask = taskMap.get(taskName);
    var processorList = meshineryTask.getProcessorList();

    ComposableFutureUtils.combineProcessors(
        processorList,
        context,
        executorService,
        MDC.getCopyOfContextMap(),
        meshineryTask.getTaskData()
    ).get();

    MDC.clear();
  }

}
