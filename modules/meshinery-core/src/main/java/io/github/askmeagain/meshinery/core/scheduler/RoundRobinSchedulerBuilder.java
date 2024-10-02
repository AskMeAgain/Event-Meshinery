package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.InputSourceDecorator;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.other.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class RoundRobinSchedulerBuilder {

  private final List<Consumer<RoundRobinScheduler>> shutdownHook = new ArrayList<>();
  private final List<ProcessorDecorator<?>> processorDecorators = new ArrayList<>();
  private final List<InputSourceDecorator<?, ?>> inputSourceDecorators = new ArrayList<>();
  private final List<Consumer<RoundRobinScheduler>> startupHooks = new ArrayList<>();

  private boolean isBatchJob;
  private int backpressureLimit = 200;
  private int gracePeriodMilliseconds = 2000;
  private boolean gracefulShutdownOnError = true;
  private final List<MeshineryTask> tasks = new ArrayList<>();

  private DataInjectingExecutorService executorService = new DataInjectingExecutorService(
      "default-virtual-thread-pool",
      Executors.newVirtualThreadPerTaskExecutor()
  );

  public RoundRobinSchedulerBuilder properties(MeshineryCoreProperties meshineryCoreProperties) {
    return backpressureLimit(meshineryCoreProperties.getBackpressureLimit())
        .gracefulShutdownOnError(meshineryCoreProperties.isShutdownOnError())
        .gracePeriodMilliseconds(meshineryCoreProperties.getGracePeriodMilliseconds())
        .batchJob(meshineryCoreProperties.isBatchJob());
  }

  /**
   * Add a task to the RoundRobinScheduler
   *
   * @param task to be added to the scheduler
   * @return returns itself for builder pattern
   */
  public RoundRobinSchedulerBuilder task(MeshineryTask<?, ?> task) {
    tasks.add(task);
    return this;
  }

  /**
   * Add a list of tasks to the RoundRobinScheduler
   *
   * @param task to be added to the scheduler
   * @return returns itself for builder pattern
   */
  public RoundRobinSchedulerBuilder task(List<MeshineryTask<?, ?>> task) {
    tasks.addAll(task);
    return this;
  }

  /**
   * backpressure limit how many current task runs will be held in memory.
   * There is a potential data loss when the scheduler fails while data is in memory, due to the
   * input source working like a queue with at most once guarantee. Higher number means more throughput
   * and more performance, lower number means that less data is hold back to feed into the scheduler
   * and the sources have to be queried more often
   *
   * @param backpressureLimit
   * @return itself for builder pattern
   */
  public RoundRobinSchedulerBuilder backpressureLimit(int backpressureLimit) {
    this.backpressureLimit = backpressureLimit;
    return this;
  }

  /**
   * Executor service used to schedule all the tasks.
   *
   * @param executorService
   * @return itself for builder pattern
   */
  public RoundRobinSchedulerBuilder executorService(ExecutorService executorService) {
    this.executorService = new DataInjectingExecutorService("custom-executor-service", executorService);
    return this;
  }

  /**
   * Register a processor decorator which will be applied to *all* processors. Also including
   * internal processors for writing to a source etc
   *
   * @param decorator to be registered
   * @return returns itself for builder pattern
   */
  public RoundRobinSchedulerBuilder registerProcessorDecorator(ProcessorDecorator<?> decorator) {
    this.processorDecorators.add(decorator);
    return this;
  }

  /**
   * Register a list of processor decorators which will be applied to *all* processors. Also including
   * internal processors for writing to a source etc
   *
   * @param decorators to be registered
   * @return returns itself for builder pattern
   */
  public RoundRobinSchedulerBuilder registerProcessorDecorator(
      List<ProcessorDecorator<? extends MeshineryDataContext>> decorators
  ) {
    this.processorDecorators.addAll(decorators);
    return this;
  }

  /**
   * register an list of input source decorators
   *
   * @param decorators
   * @return
   */
  public RoundRobinSchedulerBuilder registerInputSourceDecorator(
      List<InputSourceDecorator<?, ? extends MeshineryDataContext>> decorators
  ) {
    this.inputSourceDecorators.addAll(decorators);
    return this;
  }

  /**
   * register an input source decorator
   *
   * @param decorator
   * @return
   */
  public RoundRobinSchedulerBuilder registerInputSourceDecorator(InputSourceDecorator<?, ?> decorator) {
    this.inputSourceDecorators.add(decorator);
    return this;
  }

  /**
   * registers a shutdown hook that runs before the round robin scheduler is shutdown
   *
   * @param shutdownHooks list of hooks
   * @return returns itself for builder pattern
   */
  public RoundRobinSchedulerBuilder registerShutdownHook(List<? extends Consumer<RoundRobinScheduler>> shutdownHooks) {
    this.shutdownHook.addAll(shutdownHooks);
    return this;
  }

  /**
   * Register a startup hook that runs before the round robin scheduler is started
   *
   * @param startupHook list of hooks
   * @return returns itself for builder pattern
   */
  public RoundRobinSchedulerBuilder registerStartupHook(List<? extends Consumer<RoundRobinScheduler>> startupHook) {
    this.startupHooks.addAll(startupHook);
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
        startupHooks,
        gracefulShutdownOnError,
        gracePeriodMilliseconds,
        executorService
    );
  }
}