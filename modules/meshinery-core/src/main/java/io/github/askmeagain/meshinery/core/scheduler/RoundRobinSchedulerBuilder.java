package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.SneakyThrows;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class RoundRobinSchedulerBuilder<K, C extends MeshineryDataContext> {

  List<? extends Consumer<RoundRobinScheduler<K, C>>> shutdownHook = Collections.emptyList();
  List<ProcessorDecorator<C>> processorDecorators = Collections.emptyList();
  List<InputSourceDecoratorFactory> connectorDecoratorFactories = Collections.emptyList();
  List<? extends Consumer<RoundRobinScheduler<K, C>>> startupHook = Collections.emptyList();
  List<? extends Consumer<C>> preTaskRunHooks = Collections.emptyList();
  List<? extends Consumer<C>> postTaskRunHooks = Collections.emptyList();

  int backpressureLimit = 200;
  int gracePeriodMilliseconds = 2000;
  private DataInjectingExecutorService executorService = new DataInjectingExecutorService(
      "default-virtual-thread-pool",
      Executors.newVirtualThreadPerTaskExecutor()
  );
  boolean isBatchJob;
  List<MeshineryTask> tasks = new ArrayList<>();
  boolean gracefulShutdownOnError = true;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinSchedulerBuilder<K, C> properties(MeshineryCoreProperties meshineryCoreProperties) {
    return backpressureLimit(meshineryCoreProperties.getBackpressureLimit())
        .gracefulShutdownOnError(meshineryCoreProperties.isShutdownOnError())
        .gracePeriodMilliseconds(meshineryCoreProperties.getGracePeriodMilliseconds())
        .isBatchJob(meshineryCoreProperties.isBatchJob());
  }

  public RoundRobinSchedulerBuilder<K, C> task(MeshineryTask task) {
    tasks.add(task);
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> backpressureLimit(int backpressureLimit) {
    this.backpressureLimit = backpressureLimit;
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> executorService(ExecutorService executorService) {
    this.executorService = new DataInjectingExecutorService("custom-executor-service", executorService);
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> tasks(List<MeshineryTask> task) {
    tasks.addAll(task);
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> registerProcessorDecorators(
      List<ProcessorDecorator<C>> processorDecorators
  ) {
    this.processorDecorators = processorDecorators;
    return this;
  }

  /**
   * Connector Decorators registered via this method will only be applied to the
   * {@link MeshineryInputSource}
   * part of the connector due to technical limits. The
   * {@link MeshineryOutputSource} WONT be decorated.
   *
   * @param connectorDecoratorFactories list of decorated factories
   * @return returns this
   */
  public RoundRobinSchedulerBuilder<K, C> registerConnectorDecorators(
      List<InputSourceDecoratorFactory> connectorDecoratorFactories
  ) {
    this.connectorDecoratorFactories = connectorDecoratorFactories;
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> registerShutdownHook(
      List<? extends Consumer<RoundRobinScheduler<K, C>>> shutdownHook
  ) {
    this.shutdownHook = shutdownHook;
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> registerStartupHook(
      List<? extends Consumer<RoundRobinScheduler<K, C>>> startupHook
  ) {
    this.startupHook = startupHook;
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> registerPreTaskRunHook(
      List<? extends Consumer<C>> preTaskRunHooks
  ) {
    this.preTaskRunHooks = preTaskRunHooks;
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> registerPostTaskRunHook(
      List<? extends Consumer<C>> postTaskRunHooks
  ) {
    this.postTaskRunHooks = postTaskRunHooks;
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> gracefulShutdownOnError(boolean gracefulShutdownOnError) {
    this.gracefulShutdownOnError = gracefulShutdownOnError;
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> isBatchJob(boolean flag) {
    isBatchJob = flag;
    return this;
  }

  public RoundRobinSchedulerBuilder<K, C> gracePeriodMilliseconds(int gracePeriodMilliseconds) {
    this.gracePeriodMilliseconds = gracePeriodMilliseconds;
    return this;
  }

  @SneakyThrows
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinScheduler<K, C> build() {

    //verifying tasks
    tasks.forEach(MeshineryTask::verifyTask);

    var fixedTasks = tasks.stream()
        .map(task -> task.addInputSourceDecorators(connectorDecoratorFactories))
        .map(task -> task.addProcessorDecorators(processorDecorators))
        .toList();

    return new RoundRobinScheduler<>(
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