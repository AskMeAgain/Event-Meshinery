package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskReplayFactory;
import java.util.List;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

@Configuration
@RequiredArgsConstructor
@Import({DataContextInjectApiController.class, ApplicationStartHookConfiguration.class})
@EnableConfigurationProperties(MeshineryCoreProperties.class)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryAutoConfiguration {

  private final MeshineryCoreProperties meshineryCoreProperties;

  @Lazy
  @Bean
  public TaskReplayFactory taskReplayFactory(List<MeshineryTask<?, ?>> tasks) {
    return new TaskReplayFactory(tasks, Executors.newSingleThreadExecutor());
  }

  @Bean
  @ConditionalOnProperty(prefix = "meshinery.core", name = "batch-job", havingValue = "true")
  public BatchJobTimingHooks batchJobTiming() {
    return new BatchJobTimingHooks();
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "meshinery.core",
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
      List<ProcessorDecorator<DataContext, DataContext>> processorDecorators
  ) {
    return RoundRobinScheduler.builder()
        .isBatchJob(meshineryCoreProperties.isBatchJob())
        .registerShutdownHook(shutdownHook)
        .registerStartupHook(startupHook)
        .registerDecorators(processorDecorators)
        .gracefulShutdownOnError(meshineryCoreProperties.isShutdownOnError())
        .gracePeriodMilliseconds(meshineryCoreProperties.getGracePeriodMilliseconds())
        .tasks(tasks)
        .build();
  }
}