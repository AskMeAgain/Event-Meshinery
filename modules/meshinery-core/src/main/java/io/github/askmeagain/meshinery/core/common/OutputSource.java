package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface OutputSource<K, C extends DataContext> {

  String getName();

  void writeOutput(K key, C output);

  default TaskData getTaskData() {
    return TaskData.getTaskData();
  }
}
