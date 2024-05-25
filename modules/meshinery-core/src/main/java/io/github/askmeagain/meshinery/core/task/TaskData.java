package io.github.askmeagain.meshinery.core.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;


@NoArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
@AllArgsConstructor
public class TaskData {

  private static final ThreadLocal<TaskData> taskData = ThreadLocal.withInitial(TaskData::new);

  @With(AccessLevel.PRIVATE)
  private Properties properties = new Properties();

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static TaskData ofPropertyList(String[] kvList) {
    var taskData = new TaskData();
    for (var kv : kvList) {
      var arr = kv.split("=");
      taskData = taskData.with(arr[0], arr[1]);
    }
    return taskData;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static TaskData getTaskData() {
    return taskData.get();
  }

  public static void setTaskData(TaskData taskData) {
    TaskData.taskData.set(taskData);
  }

  public static void clearTaskData() {
    TaskData.taskData.remove();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public TaskData with(String key, String value) {
    var newProperties = new Properties();
    newProperties.putAll(properties);
    var list = (List<String>) newProperties.get(key);
    if (list == null) {
      list = new ArrayList<>();
    }
    list.add(value);
    newProperties.put(key, list);
    return this.withProperties(newProperties);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public TaskData replace(String key, String value) {
    var newProperties = new Properties();
    newProperties.putAll(properties);
    var list = new ArrayList<>();
    list.add(value);
    newProperties.put(key, list);
    return this.withProperties(newProperties);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public Properties getProperties() {
    var newProperties = new Properties();
    newProperties.putAll(properties);
    return newProperties;
  }

  public List<String> get(String key) {
    return (List<String>) properties.get(key);
  }

  public String getSingle(String key) {
    var list = (List<String>) properties.get(key);
    return list.get(0);
  }

  public boolean has(String key) {
    return properties.containsKey(key);
  }
}
