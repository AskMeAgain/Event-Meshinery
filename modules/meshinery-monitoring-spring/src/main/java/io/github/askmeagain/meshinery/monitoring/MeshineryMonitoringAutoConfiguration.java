package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.CustomizeStartupHook;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Configuration
@ConditionalOnMissingBean(MeshineryMonitoringAutoConfiguration.class)
public class MeshineryMonitoringAutoConfiguration {

  @Bean
  TimingDecorator<? extends DataContext, ?> timingDecorator() {
    return new TimingDecorator<>();
  }

  @Bean
  CustomizeStartupHook startupHook() {
    return scheduler -> {
      MeshineryMonitoringService.registerNewGauge(
          "TodoQueue",
          "Number of currently waiting tasks in queue.",
          () -> (double) scheduler.getTodoQueue().size()
      );
      MeshineryMonitoringService.registerNewGauge(
          "todoqueue_open_capacity",
          "Number of possible items in todo queue.",
          () -> (double) scheduler.getBackpressureLimit() - scheduler.getTodoQueue().size()
      );
      MeshineryMonitoringService.registerNewGauge(
          "registered_tasks",
          "Number of registered tasks.",
          () -> (double) scheduler.getTasks().size()
      );

      var gauge = MeshineryMonitoringService.createGauge(
          "processors_per_task",
          "Number of registered processors.",
          "task_name"
      );

      scheduler.getTasks().forEach(task -> {
        var size = (double) task.getProcessorList().size();
        var newGauge = gauge.labels(task.getTaskName());
        newGauge.set(size);
      });
    };
  }
}
