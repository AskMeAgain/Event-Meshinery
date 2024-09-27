package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskVerifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class RoundRobinSchedulerBuilder {

  private List<? extends Consumer<RoundRobinScheduler>> shutdownHook = Collections.emptyList();
  private List<ProcessorDecorator<? extends MeshineryDataContext>> processorDecorators = Collections.emptyList();
  private List<InputSourceDecoratorFactory<?, ? extends MeshineryDataContext>> connectorDecoratorFactories =
      Collections.emptyList();
  private List<? extends Consumer<RoundRobinScheduler>> startupHook = Collections.emptyList();

  private int backpressureLimit = 200;
  private int gracePeriodMilliseconds = 2000;
  private boolean isBatchJob;
  private boolean gracefulShutdownOnError = true;
  private List<MeshineryTask> tasks = new ArrayList<>();

  private DataInjectingExecutorService executorService = new DataInjectingExecutorService(
      "default-virtual-thread-pool",
      Executors.newVirtualThreadPerTaskExecutor()
  );

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinSchedulerBuilder properties(MeshineryCoreProperties meshineryCoreProperties) {
    return backpressureLimit(meshineryCoreProperties.getBackpressureLimit())
        .gracefulShutdownOnError(meshineryCoreProperties.isShutdownOnError())
        .gracePeriodMilliseconds(meshineryCoreProperties.getGracePeriodMilliseconds())
        .batchJob(meshineryCoreProperties.isBatchJob());
  }

  public RoundRobinSchedulerBuilder task(MeshineryTask task) {
    tasks.add(task);
    return this;
  }

  public RoundRobinSchedulerBuilder backpressureLimit(int backpressureLimit) {
    this.backpressureLimit = backpressureLimit;
    return this;
  }

  public RoundRobinSchedulerBuilder executorService(ExecutorService executorService) {
    this.executorService = new DataInjectingExecutorService("custom-executor-service", executorService);
    return this;
  }

  public RoundRobinSchedulerBuilder tasks(List<MeshineryTask<?, ?>> task) {
    tasks.addAll(task);
    return this;
  }

  public RoundRobinSchedulerBuilder registerProcessorDecorators(
      List<ProcessorDecorator<? extends MeshineryDataContext>> decorators
  ) {
    this.processorDecorators = decorators;
    return this;
  }

  public RoundRobinSchedulerBuilder registerDecorators(
      List<InputSourceDecoratorFactory<?, ? extends MeshineryDataContext>> factories
  ) {
    this.connectorDecoratorFactories = factories;
    return this;
  }

  public RoundRobinSchedulerBuilder registerShutdownHook(List<? extends Consumer<RoundRobinScheduler>> shutdownHook) {
    this.shutdownHook = shutdownHook;
    return this;
  }

  public RoundRobinSchedulerBuilder registerStartupHook(List<? extends Consumer<RoundRobinScheduler>> startupHook) {
    this.startupHook = startupHook;
    return this;
  }

  public RoundRobinSchedulerBuilder gracefulShutdownOnError(boolean gracefulShutdownOnError) {
    this.gracefulShutdownOnError = gracefulShutdownOnError;
    return this;
  }

  public RoundRobinSchedulerBuilder batchJob(boolean flag) {
    isBatchJob = flag;
    return this;
  }

  public RoundRobinSchedulerBuilder gracePeriodMilliseconds(int gracePeriodMilliseconds) {
    this.gracePeriodMilliseconds = gracePeriodMilliseconds;
    return this;
  }

  public RoundRobinScheduler build() {
    //verifying tasks
    //TODO combine this
    MeshineryTaskVerifier.verifyTasks(tasks);
    tasks.forEach(MeshineryUtils::verifyTask);

    //adding the scheduler decorators
    var fixedTasks = tasks.stream()
        .map(task -> task.toBuilder()
            .registerInputSourceDecorator(connectorDecoratorFactories)
            .build())
        .map(task -> task.toBuilder()
            .registerProcessorDecorator(processorDecorators)
            .build())
        .toList();

    return new RoundRobinScheduler(
        fixedTasks,
        backpressureLimit,
        isBatchJob,
        shutdownHook,
        startupHook,
        gracefulShutdownOnError,
        gracePeriodMilliseconds,
        executorService
    );
  }
}