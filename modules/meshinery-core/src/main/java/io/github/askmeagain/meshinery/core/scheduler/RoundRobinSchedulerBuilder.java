package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.InputSourceDecorator;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class RoundRobinSchedulerBuilder {

  private List<? extends Consumer<RoundRobinScheduler>> shutdownHook = Collections.emptyList();
  private List<ProcessorDecorator<? extends MeshineryDataContext>> processorDecorators = Collections.emptyList();
  private List<InputSourceDecorator<?, ? extends MeshineryDataContext>> inputSourceDecorators =
      Collections.emptyList();
  private List<? extends Consumer<RoundRobinScheduler>> startupHook = Collections.emptyList();

  private boolean isBatchJob;
  private int backpressureLimit = 200;
  private int gracePeriodMilliseconds = 2000;
  private boolean gracefulShutdownOnError = true;
  private final List<MeshineryTask> tasks = new ArrayList<>();

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
      List<InputSourceDecorator<?, ? extends MeshineryDataContext>> factories
  ) {
    this.inputSourceDecorators = factories;
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

  /**
   * If an error happens, the app will shutdown itself if this is set to true.
   *
   * @param gracefulShutdownOnError
   * @return
   */
  public RoundRobinSchedulerBuilder gracefulShutdownOnError(boolean gracefulShutdownOnError) {
    this.gracefulShutdownOnError = gracefulShutdownOnError;
    return this;
  }

  /**
   * Transforms this app into a batch job. It will shutdown itself once all input sources are exhausted and the
   * grace period is over
   *
   * @param flag
   * @return
   */
  public RoundRobinSchedulerBuilder batchJob(boolean flag) {
    isBatchJob = flag;
    return this;
  }

  /**
   * Only used when batchJob = true. App will shutdown itself once all input sources are exhausted and the
   * grace period is over
   *
   * @param gracePeriodMilliseconds
   * @return
   */
  public RoundRobinSchedulerBuilder gracePeriodMilliseconds(int gracePeriodMilliseconds) {
    this.gracePeriodMilliseconds = gracePeriodMilliseconds;
    return this;
  }

  public RoundRobinScheduler build() {
    log.info("Starting Scheduler with following Tasks: {}", MeshineryUtils.getAndVerifyTaskList(tasks));
    log.info("Starting Scheduler with following Input Source: {}", MeshineryUtils.getInputSources(tasks));
    log.info("Starting Scheduler with following Output Source: {}", MeshineryUtils.getOutputSources(tasks));

    MeshineryUtils.verifyTasks(tasks);

    //adding the scheduler decorators
    var fixedTasks = tasks.stream()
        .map(task -> task.toBuilder()
            .registerInputSourceDecorator(inputSourceDecorators)
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