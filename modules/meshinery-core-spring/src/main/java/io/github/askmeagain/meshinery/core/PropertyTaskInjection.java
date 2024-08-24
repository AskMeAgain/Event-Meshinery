package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class PropertyTaskInjection {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static List<MeshineryTask> injectProperties(
      List<MeshineryTask> tasks,
      MeshineryCoreProperties coreProperties
  ) {

    var newList = new ArrayList<MeshineryTask>();

    for (var task : tasks) {
      if (coreProperties.getTaskProperties().containsKey(task.getTaskName())) {
        var newTaskData = Optional.ofNullable(task.getTaskData()).orElse(new TaskData());
        for (var kv : coreProperties.getTaskProperties().get(task.getTaskName()).entrySet()) {
          newTaskData = newTaskData.with(kv.getKey(), kv.getValue());
        }
        newList.add(task.withTaskData(newTaskData));
      } else {
        newList.add(task);
      }
    }

    return newList;
  }
}
