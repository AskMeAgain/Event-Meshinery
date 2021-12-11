package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.processors.DynamicOutputProcessor;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskVerifier;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.task.TaskDataProperties;
import io.github.askmeagain.meshinery.core.task.TaskRun;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.MODULE)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class RoundRobinScheduler {

  public static final int GRACE_PERIOD = 2000;
  private final List<MeshineryTask<?, ?>> tasks;

  private final ConcurrentLinkedQueue<TaskRun> outputQueue = new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<ConnectorKey> inputQueue = new ConcurrentLinkedQueue<>();

  private final Map<ConnectorKey, MeshineryTask<?, ?>> taskRunLookupMap = new HashMap<>();

  private final int backpressureLimit;
  private final boolean isBatchJob;
  private final List<? extends Consumer<RoundRobinScheduler>> shutdownHook;
  private final List<? extends Consumer<RoundRobinScheduler>> startupHook;
  private final List<ProcessorDecorator<DataContext, DataContext>> processorDecorator;
  private final boolean gracefulShutdownOnError;
  private boolean internalShutdown = false;
  private final AtomicInteger outputDone = new AtomicInteger();
  private final AtomicInteger inputDone = new AtomicInteger();
  private final Set<ExecutorService> executorServices = new HashSet<>();

  public static SchedulerBuilder builder() {
    return new SchedulerBuilder();
  }

  @SneakyThrows
  RoundRobinScheduler start() {
    MeshineryTaskVerifier.verifyTasks(tasks);

    //task gathering
    tasks.forEach(task -> executorServices.add(task.getExecutorService()));
    createLookupMap();

    //the producer
    var inputExecutor = new DataInjectingExecutorService("input-executor", Executors.newSingleThreadExecutor());
    executorServices.add(inputExecutor);
    inputExecutor.execute(() -> createInputScheduler(inputExecutor));

    Thread.sleep(100);

    //the worker
    var taskExecutor = new DataInjectingExecutorService("output-executor", Executors.newSingleThreadExecutor());
    executorServices.add(taskExecutor);
    taskExecutor.execute(() -> runWorker(taskExecutor));

    startupHook.forEach(hook -> hook.accept(this));

    return this;
  }

  private void createLookupMap() {
    for (var task : tasks) {
      var connectorKey = ConnectorKey.builder()
          .connector((MeshineryConnector<Object, DataContext>) task.getInputConnector())
          .key(task.getInputKey())
          .build();

      taskRunLookupMap.put(connectorKey, task);
    }
  }

  public void gracefulShutdown() {
    log.info("Graceful shutdown");
    internalShutdown = true;
  }

  @SneakyThrows
  private void createInputScheduler(ExecutorService executor) {
    log.info("Starting input worker thread");

    inputQueue.addAll(fillQueueFromTasks());

    while (!executor.isShutdown() && !internalShutdown && (!outputQueue.isEmpty() || !inputQueue.isEmpty())) {
      if (backpressureLimit <= outputQueue.size()) {
        log.warn("Waiting because of backpressure");
        continue;
      }

      while (!inputQueue.isEmpty()) {
        var work = inputQueue.peek();

        var newInputs = queryTaskRuns(work);

        outputQueue.addAll(newInputs); //in this order so any queue is always filled
        inputQueue.remove();
      }

      if (outputQueue.isEmpty() && isBatchJob) {
        log.info("Grace period for input thread");
        Thread.sleep(GRACE_PERIOD);
        if (outputQueue.isEmpty()) {
          log.info("One iteration with no work and outputqueue is not working on anything (is empty)");
          break;
        }
      }

      inputQueue.addAll(fillQueueFromTasks());
    }

    MDC.clear();
    log.info("Input scheduler gracefully shutdown");
  }

  private List<ConnectorKey> fillQueueFromTasks() {
    return tasks.stream()
        .map(MeshineryTask::getConnectorKey)
        .toList();
  }

  private List<TaskRun> queryTaskRuns(ConnectorKey work) {
    try {
      if (!taskRunLookupMap.containsKey(work)) {
        return Collections.emptyList();
      }

      return taskRunLookupMap.get(work).getNewTaskRuns();
    } catch (Exception e) {
      if (gracefulShutdownOnError) {
        log.error("Error while requesting new input data. Shutting down scheduler", e);
        gracefulShutdown();
        return Collections.emptyList();
      }
      throw e;
    }
  }

  @SneakyThrows
  private void runWorker(ExecutorService executor) {
    var begin = Instant.now();
    log.info("Starting processing worker thread");

    //we use this label to break out of the task in case we dont want to work on it
    newTask:
    while (!executor.isShutdown() && !internalShutdown) {
      MDC.clear();

      var currentTask = outputQueue.peek();

      if (currentTask == null && inputQueue.isEmpty() && isBatchJob) {
        Thread.sleep(GRACE_PERIOD);
        if (inputQueue.isEmpty()) {
          break;
        }
      }

      if (currentTask == null) {
        continue;
      }

      MDC.put(TaskDataProperties.TASK_NAME, currentTask.getTaskName());
      MDC.put(TaskDataProperties.UID, currentTask.getId());
      while (currentTask.getFuture().isDone()) {
        var queue = currentTask.getQueue();

        //we stop if we reached the end of the queue
        if (queue.isEmpty()) {
          outputQueue.remove();
          continue newTask;
        }

        if (currentTask.getFuture().isCompletedExceptionally()) {
          currentTask.getFuture().whenComplete((context, ex) -> log.error("Processor completed with error", ex));
          var handleError = currentTask.getHandleError();
          var handledFuture = currentTask.getFuture()
              .handle((context, throwable) -> handleError.apply(throwable));

          currentTask = currentTask.withFuture(handledFuture);
        }

        var nextProcessor = queue.remove();

        var context = currentTask.getFuture().get();

        //we stop if the context is null
        if (context == null) {
          outputQueue.remove();
          MDC.clear();
          continue newTask;
        }
        var resultFuture = getResultFuture(currentTask, nextProcessor, context);

        currentTask = currentTask.withFuture(resultFuture);
      }

      outputQueue.add(currentTask); //this way, so we have always atleast 1 item in queue to signal we have work todo
      outputQueue.remove();
    }

    log.info("Reached end of Queue. Shutting down now");


    for (var executorService : executorServices) {
      if (!executorService.isShutdown()) {
        executorService.shutdown();
      }
    }

    var diff = Duration.between(begin, Instant.now());
    log.info("Seconds: " + diff.minusMillis(GRACE_PERIOD).getSeconds());
    log.info("Grace period: " + GRACE_PERIOD);


    shutdownHook.forEach(hook -> hook.accept(this));
  }

  private CompletableFuture<DataContext> getResultFuture(
      TaskRun taskRun,
      MeshineryProcessor<DataContext, DataContext> nextProcessor,
      DataContext context
  ) {
    try {
      TaskData.setTaskData(taskRun.getTaskData());
      var decoratedProcessor = MeshineryUtils.applyDecorators(nextProcessor, processorDecorator);
      return decoratedProcessor.processAsync(context, taskRun.getExecutorService())
          .thenApply(c -> {
            if (nextProcessor instanceof DynamicOutputProcessor dynamicOutputProcessor) {
              inputQueue.add(ConnectorKey.builder()
                  .connector(dynamicOutputProcessor.outputSource())
                  .key(dynamicOutputProcessor.keyMethod().apply(context))
                  .build());
            }
            return c;
          });
    } catch (Exception e) {
      if (gracefulShutdownOnError) {
        log.error(
            "Error while preparing/processing processor '{}'. Shutting down gracefully",
            nextProcessor.getClass().getSimpleName(),
            e
        );
        gracefulShutdown();
        return CompletableFuture.completedFuture(null);
      }

      throw e;
    }
  }
}
