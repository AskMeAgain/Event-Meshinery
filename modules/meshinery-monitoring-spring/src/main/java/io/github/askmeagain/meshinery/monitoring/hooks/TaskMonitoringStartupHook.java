package io.github.askmeagain.meshinery.monitoring.hooks;

import io.github.askmeagain.meshinery.core.hooks.CustomizeStartupHook;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.monitoring.MeshineryMonitoringService;

public class TaskMonitoringStartupHook implements CustomizeStartupHook {

  @Override
  public void accept(RoundRobinScheduler scheduler) {
    MeshineryMonitoringService.createGauge(
        "queue",
        "Number of currently waiting tasks in queue.",
        () -> (double) scheduler.getOutputQueue().size()
    );
    MeshineryMonitoringService.createGauge(
        "queue_open_capacity",
        "Number of possible items in todo queue.",
        () -> (double) scheduler.getBackpressureLimit() - scheduler.getOutputQueue().size()
    );
    MeshineryMonitoringService.createGauge(
        "registered_tasks",
        "Number of registered tasks.",
        () -> (double) scheduler.getTasks().size()
    );
    var processorListGauge = MeshineryMonitoringService.createGauge(
        "processors_per_task",
        "Number of registered processors.",
        "task_name"
    );
    scheduler.getTasks().forEach(task -> {
      var size = (double) task.getProcessorList().size();
      processorListGauge.labels(task.getTaskName())
          .set(size);
    });
  }
}
