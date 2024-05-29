package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.TaskData;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface MeshineryOutputSource<K, C extends MeshineryDataContext> {

  String getName();

  void writeOutput(K key, C output, TaskData taskData);

  default TaskData getTaskData() {
    return TaskData.getTaskData();
  }
}
