package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.List;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface InputSource<K, I extends DataContext> {

  String getName();

  List<I> getInputs(List<K> key);

  default TaskData addToTaskData(TaskData taskData) {
    return taskData;
  }

}
