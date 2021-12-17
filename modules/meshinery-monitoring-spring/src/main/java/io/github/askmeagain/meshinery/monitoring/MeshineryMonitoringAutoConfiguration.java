package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.CustomizeStartupHook;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.prometheus.client.Gauge;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.askmeagain.meshinery.monitoring.MeshineryMonitoringUtils.getNameByExecutorAndTasks;

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

      var executorPerTaskMap = MeshineryMonitoringUtils.createExecutorPerTaskMap(roundRobinScheduler.getTasks());

      var executorAssignmentGauge = Gauge.build()
          .name("executor")
          .help("Executors and their registered tasks")
          .labelNames("executor", "task_name")
          .register(MeshineryMonitoringService.registry);

      executorPerTaskMap.forEach((executor, tasks) -> {
        tasks.forEach(task -> {
          executorAssignmentGauge.labels(String.valueOf(executor.hashCode()), task.getTaskName());
        });
      });

      var maxThreadGauge = Gauge.build()
          .name("executor_max_threads")
          .help("Max number of threads on each executor")
          .labelNames("executor")
          .register(MeshineryMonitoringService.registry);

      roundRobinScheduler.getExecutorServices()
          .forEach(executorService -> {
            var dataInjectingExecutorService = (DataInjectingExecutorService) executorService;

            Gauge.Child child;
            var executorService1 = dataInjectingExecutorService.getExecutorService();
            if (executorService1 instanceof ThreadPoolExecutor castedAgain) {
              child = new Gauge.Child() {
                @Override
                public double get() {
                  return castedAgain.getMaximumPoolSize();
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

            var name = getNameByExecutorAndTasks(executorPerTaskMap, dataInjectingExecutorService);

            maxThreadGauge.setChild(child, name);
          });

      var gauge = Gauge.build()
          .name("executor_active_threads")
          .help("Available Threads on each executor")
          .labelNames("executor")
          .register(MeshineryMonitoringService.registry);

      roundRobinScheduler.getExecutorServices()
          .forEach(executorService -> {
            Gauge.Child child;

            var dataInjectingExecutorService = (DataInjectingExecutorService) executorService;
            var innerExecutorService = dataInjectingExecutorService.getExecutorService();

            if (innerExecutorService instanceof ThreadPoolExecutor castedAgain) {
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

            var name = getNameByExecutorAndTasks(executorPerTaskMap, dataInjectingExecutorService);

            gauge.setChild(child, name);
          });
    };
  }

  @Bean
  CustomizeStartupHook taskMonitoringInformation() {
    return scheduler -> {
      MeshineryMonitoringService.createGauge(
          "todoqueue",
          "Number of currently waiting tasks in queue.",
          () -> (double) scheduler.getOutputQueue().size()
      );
      MeshineryMonitoringService.createGauge(
          "todoqueue_open_capacity",
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
    };
  }
}
