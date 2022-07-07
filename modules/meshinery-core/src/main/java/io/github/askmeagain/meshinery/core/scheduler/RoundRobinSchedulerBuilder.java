package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import lombok.SneakyThrows;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class RoundRobinSchedulerBuilder {

  List<? extends Consumer<RoundRobinScheduler>> shutdownHook = Collections.emptyList();
  List<ProcessorDecorator<DataContext, DataContext>> processorDecorators = Collections.emptyList();
  List<InputSourceDecoratorFactory> connectorDecoratorFactories = Collections.emptyList();
  List<? extends Consumer<RoundRobinScheduler>> startupHook = Collections.emptyList();
  int backpressureLimit = 200;
  int gracePeriodMilliseconds = 2000;
  boolean isBatchJob;
  List<MeshineryTask<?, ? extends DataContext>> tasks = new ArrayList<>();
  boolean gracefulShutdownOnError = true;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinSchedulerBuilder properties(MeshineryCoreProperties meshineryCoreProperties) {
    return backpressureLimit(meshineryCoreProperties.getBackpressureLimit())
        .gracefulShutdownOnError(meshineryCoreProperties.isShutdownOnError())
        .gracePeriodMilliseconds(meshineryCoreProperties.getGracePeriodMilliseconds())
        .isBatchJob(meshineryCoreProperties.isBatchJob());
  }

  public RoundRobinSchedulerBuilder task(MeshineryTask<?, ? extends DataContext> task) {
    tasks.add(task);
    return this;
  }

  public RoundRobinSchedulerBuilder backpressureLimit(int backpressureLimit) {
    this.backpressureLimit = backpressureLimit;
    return this;
  }

  public RoundRobinSchedulerBuilder tasks(List<MeshineryTask<?, ? extends DataContext>> task) {
    tasks.addAll(task);
    return this;
  }

  public RoundRobinSchedulerBuilder registerProcessorDecorators(
      List<ProcessorDecorator<DataContext, DataContext>> processorDecorators
  ) {
    this.processorDecorators = processorDecorators;
    return this;
  }

  /**
   * Connector Decorators registered via this method will only be applied to the
   * {@link io.github.askmeagain.meshinery.core.common.InputSource}
   * part of the connector due to technical limits. The
   * {@link io.github.askmeagain.meshinery.core.common.OutputSource} WONT be decorated.
   *
   * @param connectorDecoratorFactories list of decorated factories
   * @return returns this
   */
  public RoundRobinSchedulerBuilder registerConnectorDecorators(
      List<InputSourceDecoratorFactory> connectorDecoratorFactories
  ) {
    this.connectorDecoratorFactories = connectorDecoratorFactories;
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

  public RoundRobinSchedulerBuilder isBatchJob(boolean flag) {
    isBatchJob = flag;
    return this;
  }

  public RoundRobinSchedulerBuilder gracePeriodMilliseconds(int gracePeriodMilliseconds) {
    this.gracePeriodMilliseconds = gracePeriodMilliseconds;
    return this;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinScheduler buildAndStart() {
    return build().start();
  }

  @SneakyThrows
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public RoundRobinScheduler build() {

    //verifying tasks
    tasks.forEach(MeshineryTask::verifyTask);

    var fixedTasks = MeshineryUtils.decorateMeshineryTasks(tasks, connectorDecoratorFactories);

    return new RoundRobinScheduler(
        (List<MeshineryTask<?, ?>>) fixedTasks,
        backpressureLimit,
        isBatchJob,
        shutdownHook,
        startupHook,
        processorDecorators,
        gracefulShutdownOnError,
        gracePeriodMilliseconds
    );
  }
}