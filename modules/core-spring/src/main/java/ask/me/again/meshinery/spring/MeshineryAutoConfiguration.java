package ask.me.again.meshinery.spring;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.ProcessorDecorator;
import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.task.MeshineryTask;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryAutoConfiguration {

  @Value("${meshinery.batch-job:false}")
  private boolean isBatchJob;

  @Bean
  @ConditionalOnProperty(
      prefix = "meshinery",
      name = "shutdown-on-finished",
      havingValue = "true",
      matchIfMissing = true)
  CustomizeShutdownHook shutdownHook(ApplicationContext context) {
    return scheduler -> ((ConfigurableApplicationContext) context).close();
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinScheduler roundRobinScheduler(
      List<MeshineryTask<?, ?>> tasks,
      List<CustomizeShutdownHook> shutdownHook,
      List<CustomizeStartupHook> startupHook,
      List<ProcessorDecorator<Context, Context>> processorDecorators
  ) {
    return RoundRobinScheduler.builder()
        .isBatchJob(isBatchJob)
        .registerShutdownHook(shutdownHook)
        .registerStartupHook(startupHook)
        .registerDecorators(processorDecorators)
        .tasks(tasks)
        .buildAndStart();
  }
}