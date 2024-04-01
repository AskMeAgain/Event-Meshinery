package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Input Source interface. An inputSource enables you to get data from a MeshineryConnector by polling the source.
 *
 * @param <K> Event key type
 * @param <I> context type
 */
public interface InputSource<K, I extends DataContext> {

  String getName();

  List<I> getInputs(List<K> key);

  default TaskData addToTaskData(TaskData taskData) {
    return taskData;
  }

  default CompletableFuture<I> commit(I context) {
    return CompletableFuture.completedFuture(context);
  }

}
