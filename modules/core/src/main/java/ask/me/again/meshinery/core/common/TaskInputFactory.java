package ask.me.again.meshinery.core.common;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TaskInputFactory {

  private final Map<String, MeshineryTask<?, ? extends Context>> taskMap;
  private final ExecutorService executorService;

  public TaskInputFactory(List<MeshineryTask<?, ? extends Context>> tasks, ExecutorService executorService) {
    this.executorService = executorService;
    this.taskMap = tasks.stream()
        .collect(Collectors.toMap(MeshineryTask::getTaskName, Function.identity()));
  }

  public <C extends Context> void writeMessage(String taskName, C context)
      throws ExecutionException, InterruptedException {

    var processorList = taskMap.get(taskName).getProcessorList();

    ComposableFutureUtils.getoCompletableFuture(processorList, context, executorService)
        .get();
  }

}
