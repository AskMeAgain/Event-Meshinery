package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.task.TaskData;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface OutputSource<K, C extends Context> {

  String getName();

  void writeOutput(K key, C output);

  default TaskData getTaskData() {
    return TaskData.getTaskData();
  }
}
