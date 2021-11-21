package ask.me.again.meshinery.monitoring;

import ask.me.again.springconfig.CustomizeStartupHook;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnMissingBean(MeshineryMonitoringAutoConfiguration.class)
public class MeshineryMonitoringAutoConfiguration {

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
    };
  }
}
