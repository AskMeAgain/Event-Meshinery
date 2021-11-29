package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.task.TaskData;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@FunctionalInterface
@SuppressWarnings("checkstyle:MissingJavadocType")
public interface MeshineryProcessor<I extends DataContext, O extends DataContext> {

  CompletableFuture<O> processAsync(I context, Executor executor);

  default TaskData addToTaskData(TaskData taskData) {
    return taskData;
  }

  default TaskData getTaskData() {
    return TaskData.getTaskData();
  }
}
