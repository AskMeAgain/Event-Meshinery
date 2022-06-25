package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Processor interface to execute the business logic of your application. An executor is passed into the processor
 * to allow for async execution. Return a completable future which signals when the work is done, to help the scheduler
 * query tasks correctly
 *
 * @param <I> Input context type
 * @param <O> Output context type. Most of the time the same as the input type
 */
@FunctionalInterface
public interface MeshineryProcessor<I extends DataContext, O extends DataContext> {

  CompletableFuture<O> processAsync(I context, Executor executor);

  default TaskData addToTaskData(TaskData taskData) {
    return taskData;
  }

  default TaskData getTaskData() {
    return TaskData.getTaskData();
  }
}
