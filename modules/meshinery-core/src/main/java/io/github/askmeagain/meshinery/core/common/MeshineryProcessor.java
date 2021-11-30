package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;
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
