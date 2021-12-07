package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.CustomizeStartupHook;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.prometheus.client.Gauge;
import java.util.concurrent.ThreadPoolExecutor;
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
  CustomizeStartupHook executorRegistration() {
    return roundRobinScheduler -> {

      var gauge = Gauge.build()
          .name("executor_active_threads")
          .help("abc test")
          .labelNames("executor")
          .register(MeshineryMonitoringService.registry);


      roundRobinScheduler.getExecutorServices()
          .forEach(executorService -> {
            var dataInjectingExecutorService = (DataInjectingExecutorService) executorService;

            Gauge.Child child;
            var executorService1 = dataInjectingExecutorService.getExecutorService();
            if (executorService1 instanceof ThreadPoolExecutor) {
              var castedAgain = (ThreadPoolExecutor) executorService1;
              child = new Gauge.Child() {
                @Override
                public double get() {
                  return castedAgain.getActiveCount();
                }
              };
            } else {
              child = new Gauge.Child() {
                @Override
                public double get() {
                  return 1;
                }
              };
            }

            gauge.setChild(child, dataInjectingExecutorService.getName());
          });
    };
  }


  @Bean
  CustomizeStartupHook taskMonitoringInformation() {
    return scheduler -> {
      MeshineryMonitoringService.createGauge(
          "todoqueue",
          "Number of currently waiting tasks in queue.",
          () -> (double) scheduler.getTodoQueue().size()
      );
      MeshineryMonitoringService.createGauge(
          "todoqueue_open_capacity",
          "Number of possible items in todo queue.",
          () -> (double) scheduler.getBackpressureLimit() - scheduler.getTodoQueue().size()
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
    };
  }
}
