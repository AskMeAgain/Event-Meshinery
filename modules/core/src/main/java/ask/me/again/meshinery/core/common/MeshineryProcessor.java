package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.task.TaskData;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@FunctionalInterface
@SuppressWarnings("checkstyle:MissingJavadocType")
public interface MeshineryProcessor<I extends Context, O extends Context> {

  CompletableFuture<O> processAsync(I context, Executor executor);

  default TaskData addToTaskData(TaskData taskData) {
    return taskData;
  }

}
