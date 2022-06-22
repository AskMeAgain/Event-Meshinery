package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PropertyTaskInjection {

  public static List<MeshineryTask<?, ? extends DataContext>> injectProperties(
      List<MeshineryTask<?, ? extends DataContext>> tasks,
      MeshineryCoreProperties coreProperties
  ) {

    var newList = new ArrayList<MeshineryTask<?, ? extends DataContext>>();

    for (var task : tasks) {
      if (coreProperties.getTaskProperties().containsKey(task.getTaskName())) {
        var newTaskData = Optional.ofNullable(task.getTaskData()).orElse(new TaskData());
        for (var kv : coreProperties.getTaskProperties().get(task.getTaskName()).entrySet()) {
          newTaskData = newTaskData.put(kv.getKey(), kv.getValue());
        }
        newList.add(task.withTaskData(newTaskData));
      } else {
        newList.add(task);
      }
    }

    return newList;
  }
}
