package io.github.askmeagain.meshinery.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.hooks.BatchJobTimingHooks;
import io.github.askmeagain.meshinery.core.hooks.CustomizePostTaskRunHook;
import io.github.askmeagain.meshinery.core.hooks.CustomizePreTaskRunHook;
import io.github.askmeagain.meshinery.core.hooks.CustomizeShutdownHook;
import io.github.askmeagain.meshinery.core.hooks.CustomizeStartupHook;
import io.github.askmeagain.meshinery.core.injecting.DataContextInjectApiController;
import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.shutdown.ShutdownApiController;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskReplayFactory;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
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
  public static DynamicMemoryConnectorRegistration dynamicMemoryConnectorRegistration(
      ApplicationContext applicationContext
  ) {
    return new DynamicMemoryConnectorRegistration(applicationContext);
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
      List<CustomizePostTaskRunHook> postTaskRunHooks,
      List<CustomizePreTaskRunHook> preTaskRunHooks,
      List<ProcessorDecorator<MeshineryDataContext, MeshineryDataContext>> processorDecorators,
      List<InputSourceDecoratorFactory> connectorDecoratorFactories,
      MeshineryCoreProperties meshineryCoreProperties,
      ExecutorService executorService
  ) {
    var appliedPropertyTasks = PropertyTaskInjection.injectProperties(tasks, meshineryCoreProperties);

    return RoundRobinScheduler.builder()
        .backpressureLimit(meshineryCoreProperties.getBackpressureLimit())
        .isBatchJob(meshineryCoreProperties.isBatchJob())
        .registerShutdownHook(shutdownHook)
        .registerStartupHook(startupHook)
        .registerPostTaskRunHook(postTaskRunHooks)
        .registerPreTaskRunHook(preTaskRunHooks)
        .registerProcessorDecorators(processorDecorators)
        .registerConnectorDecorators(connectorDecoratorFactories)
        .gracefulShutdownOnError(meshineryCoreProperties.isShutdownOnError())
        .gracePeriodMilliseconds(meshineryCoreProperties.getGracePeriodMilliseconds())
        .executorService(executorService)
        .tasks(appliedPropertyTasks)
        .build();
  }
}