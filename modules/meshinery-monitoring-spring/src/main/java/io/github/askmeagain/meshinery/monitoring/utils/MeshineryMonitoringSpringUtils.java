package io.github.askmeagain.meshinery.monitoring.utils;

import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@SuppressWarnings("checkstyle:MissingJavadocType")
@UtilityClass
public class MeshineryMonitoringSpringUtils {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static String getNameByExecutorAndTasks(
      Map<ExecutorService, List<MeshineryTask<?, ?>>> executorPerTaskMap,
      DataInjectingExecutorService dataInjectingExecutorService
  ) {
    var meshineryTasks = executorPerTaskMap.get(dataInjectingExecutorService.getExecutorService());
    return meshineryTasks == null
        ? dataInjectingExecutorService.getName()
        : "executor-" + dataInjectingExecutorService.getExecutorService().hashCode();
  }

  public static Map<ExecutorService, List<MeshineryTask<?, ?>>> createExecutorPerTaskMap(
      List<MeshineryTask<?, ?>> tasks
  ) {
    return tasks.stream()
        .collect(Collectors.groupingBy(x -> x.getExecutorService().getExecutorService()));
  }
}
