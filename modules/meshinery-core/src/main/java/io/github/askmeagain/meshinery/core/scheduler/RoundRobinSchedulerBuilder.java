package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
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
  private List<InputSourceDecoratorFactory<?, ?>> connectorDecoratorFactories = Collections.emptyList();
  private List<? extends Consumer<RoundRobinScheduler>> startupHook = Collections.emptyList();
  private List<? extends Consumer<MeshineryDataContext>> preTaskRunHooks = Collections.emptyList();
  private List<? extends Consumer<MeshineryDataContext>> postTaskRunHooks = Collections.emptyList();

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

  public RoundRobinSchedulerBuilder registerPreTaskRunHook(List<? extends Consumer<MeshineryDataContext>> hooks) {
    this.preTaskRunHooks = hooks;
    return this;
  }

  public RoundRobinSchedulerBuilder registerPostTaskRunHook(List<? extends Consumer<MeshineryDataContext>> hooks) {
    this.postTaskRunHooks = hooks;
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
    tasks.forEach(MeshineryTask::verifyTask);

    var fixedTasks = tasks.stream()
        .map(task -> task.addInputSourceDecorators(connectorDecoratorFactories))
        .map(task -> task.addProcessorDecorators(processorDecorators))
        .toList();

    return new RoundRobinScheduler(
        fixedTasks,
        backpressureLimit,
        isBatchJob,
        shutdownHook,
        startupHook,
        preTaskRunHooks,
        postTaskRunHooks,
        gracefulShutdownOnError,
        gracePeriodMilliseconds,
        executorService
    );
  }
}