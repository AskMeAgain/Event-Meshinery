package io.github.askmeagain.meshinery.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.common.InputSourceDecorator;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.hooks.BatchJobTimingHooks;
import io.github.askmeagain.meshinery.core.hooks.CustomizeShutdownHook;
import io.github.askmeagain.meshinery.core.hooks.CustomizeStartupHook;
import io.github.askmeagain.meshinery.core.injecting.DataContextInjectApiController;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.shutdown.ShutdownApiController;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskReplayFactory;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;

@Configuration
@EnableConfigurationProperties
@SuppressWarnings("checkstyle:MissingJavadocType")
@Import({DataContextInjectApiController.class, ApplicationStartHookConfiguration.class, ShutdownApiController.class})
public class MeshineryAutoConfiguration {

  @Bean
  @Validated
  @ConfigurationProperties("meshinery.core")
  public MeshineryCoreProperties meshineryCoreProperties() {
    return new MeshineryCoreProperties();
  }

  @Lazy
  @Bean
  public TaskReplayFactory taskReplayFactory(List<MeshineryTask<?, ?>> tasks) {
    return new TaskReplayFactory(tasks);
  }

  @Bean
  @ConditionalOnMissingBean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  @ConditionalOnMissingBean
  public ExecutorService executorService() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  @Bean
  public static DynamicMemoryConnectorRegistration dynamicMemoryConnectorRegistration(ApplicationContext appContext) {
    return new DynamicMemoryConnectorRegistration(appContext);
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
  @ConditionalOnMissingBean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinScheduler roundRobinScheduler(
      List<MeshineryTask<?, ?>> tasks,
      List<CustomizeShutdownHook> shutdownHook,
      List<CustomizeStartupHook> startupHook,
      List<ProcessorDecorator<? extends MeshineryDataContext>> processorDecorators,
      List<InputSourceDecorator<?, ? extends MeshineryDataContext>> connectorDecoratorFactories,
      //TODO add output decorator
      MeshineryCoreProperties meshineryCoreProperties,
      ExecutorService executorService
  ) {
    var appliedPropertyTasks = MeshineryUtils.injectProperties(tasks, meshineryCoreProperties);

    return RoundRobinScheduler.builder()
        .backpressureLimit(meshineryCoreProperties.getBackpressureLimit())
        .batchJob(meshineryCoreProperties.isBatchJob())
        .registerShutdownHook(shutdownHook)
        .registerStartupHook(startupHook)
        .registerProcessorDecorators(processorDecorators)
        .tasks(appliedPropertyTasks)
        .registerDecorators(connectorDecoratorFactories)
        .gracefulShutdownOnError(meshineryCoreProperties.isShutdownOnError())
        .gracePeriodMilliseconds(meshineryCoreProperties.getGracePeriodMilliseconds())
        .executorService(executorService)
        .build();
  }
}