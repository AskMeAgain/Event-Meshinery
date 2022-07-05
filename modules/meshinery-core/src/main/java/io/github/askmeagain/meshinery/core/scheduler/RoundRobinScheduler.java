package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.ConnectorDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.task.TaskDataProperties;
import io.github.askmeagain.meshinery.core.task.TaskRun;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

  private final List<MeshineryTask<?, ?>> tasks;
  private final int backpressureLimit;
  private final boolean isBatchJob;
  private final List<? extends Consumer<RoundRobinScheduler>> shutdownHook;
  private final List<? extends Consumer<RoundRobinScheduler>> startupHook;
  private final List<ProcessorDecorator<DataContext, DataContext>> processorDecorator;
  private final List<ConnectorDecoratorFactory<?, DataContext>> connectorDecoratorFactories;
  private final boolean gracefulShutdownOnError;
  private final int gracePeriodMilliseconds;

  private final Queue<TaskRun> outputQueue = new ConcurrentLinkedQueue<>();
  private final Queue<TaskRun> priorityQueue = new ConcurrentLinkedQueue<>();
  private final Queue<ConnectorKey> inputQueue = new ConcurrentLinkedQueue<>();

  private final Map<ConnectorKey, MeshineryTask<?, ?>> taskRunLookupMap = new HashMap<>();
  private final Set<ExecutorService> executorServices = new HashSet<>();
  private boolean gracefulShutdownTriggered = false;
  private Instant lastInputEntry;

  public static SchedulerBuilder builder() {
    return new SchedulerBuilder();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @SneakyThrows
  public RoundRobinScheduler start() {
    //setup
    tasks.forEach(task -> executorServices.add(task.getExecutorService()));
    createLookupMap();

    //the producer
    var inputExecutor = new DataInjectingExecutorService("input-executor", Executors.newSingleThreadExecutor());
    executorServices.add(inputExecutor);

    //the worker
    var taskExecutor = new DataInjectingExecutorService("output-executor", Executors.newSingleThreadExecutor());
    executorServices.add(taskExecutor);

    startupHook.forEach(hook -> hook.accept(this));

    inputExecutor.execute(() -> createInputScheduler(inputExecutor));
    Thread.sleep(100);
    taskExecutor.execute(() -> runWorker(taskExecutor));

    return this;
  }

  private void createLookupMap() {
    for (var task : tasks) {
      var connectorKey = ConnectorKey.builder()
          .connector((MeshineryConnector<Object, DataContext>) task.getInputConnector())
          .key(task.getInputKeys())
          .build();

      var decoratedInput = MeshineryUtils.applyDecorator(
          (MeshineryConnector<?, DataContext>) task.getInputConnector(), connectorDecoratorFactories
      );
      var decoratedOutput = MeshineryUtils.applyDecorator(
          (MeshineryConnector<?, DataContext>) task.getInputConnector(), connectorDecoratorFactories
      );

      var fixedTask = task.withConnector(
          decoratedInput,
          decoratedOutput
      );

      taskRunLookupMap.put(connectorKey, fixedTask);
    }
  }

  public void gracefulShutdown() {
    log.info("Graceful shutdown triggered. Shutting down all threads");
    gracefulShutdownTriggered = true;
  }

  @SneakyThrows
  private void createInputScheduler(ExecutorService executor) {
    Thread.currentThread().setName("meshinery-input");
    log.info("Starting input worker thread");

    inputQueue.addAll(fillQueueFromTasks());
    lastInputEntry = Instant.now();

    var queuesHaveWorkTodo = ((!outputQueue.isEmpty() || !priorityQueue.isEmpty()) || !inputQueue.isEmpty());

    while (!executor.isShutdown() && !gracefulShutdownTriggered && queuesHaveWorkTodo) {
      if (backpressureLimit <= outputQueue.size() + priorityQueue.size()) {
        log.info("Waiting because of backpressure");
        Thread.sleep(1000);
        continue;
      }

      while (!inputQueue.isEmpty()) {
        var work = inputQueue.peek();

        var newInputs = queryTaskRuns(work);

        outputQueue.addAll(newInputs); //in this order so any queue is always filled
        inputQueue.remove();
      }

      if (isBatchJob) {
        if (outputQueue.isEmpty() && priorityQueue.isEmpty()) {
          if (lastInputEntry.plusMillis(gracePeriodMilliseconds).isBefore(Instant.now())) {
            log.info("Grace period in input thread done.");
            gracefulShutdown();
            break;
          }
        } else {
          lastInputEntry = Instant.now();
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

  private boolean usePriorityQueue = false;

  @SneakyThrows
  private void runWorker(ExecutorService executor) {
    Thread.currentThread().setName("meshinery-output");
    log.info("Starting processing worker thread");

    //we use this label to break out of the task in case we cant work on it (not done or returns null)
    newTask:
    while (!executor.isShutdown()) {

      //toggle between queues, when priority queue is full
      usePriorityQueue = !priorityQueue.isEmpty() && !usePriorityQueue;

      var queueToUse = usePriorityQueue ? priorityQueue : outputQueue;

      var currentTask = queueToUse.peek();

      if (currentTask == null) {
        var otherQueue = !usePriorityQueue ? priorityQueue : outputQueue;
        if (otherQueue.isEmpty() && gracefulShutdownTriggered) {
          //we shutdown in case we had a graceful shutdown
          break;
        }
        continue;
      }

      while (currentTask.getFuture().isDone()) {

        var queue = currentTask.getQueue();

        //we stop if we reached the end of the queue
        if (queue.isEmpty()) {
          queueToUse.remove();
          continue newTask;
        }

        if (currentTask.getFuture().isCompletedExceptionally()) {
          currentTask.getFuture().whenComplete((context, ex) -> log.error("Processor completed with error", ex));
          var handleError = currentTask.getHandleError();
          var handledFuture = currentTask.getFuture()
              .handle((context, throwable) -> handleError.apply(throwable));

          currentTask = currentTask.withFuture(handledFuture);
        }

        var context = currentTask.getFuture().get();

        //we stop if the context is null
        if (context == null) {
          queueToUse.remove();
          continue newTask;
        }

        MDC.clear();
        MDC.put(TaskDataProperties.TASK_NAME, currentTask.getTaskName());
        MDC.put(TaskDataProperties.TASK_ID, currentTask.getId());

        var resultFuture = getResultFuture(currentTask, queue.remove(), context);

        currentTask = currentTask.withFuture(resultFuture);
      }

      //we add to priority queue, because this package got not processed
      //this order, so we have always atleast 1 item in queue to signal we have work to do
      priorityQueue.add(currentTask);
      queueToUse.remove();
    }

    for (var executorService : executorServices) {
      if (!executorService.isShutdown()) {
        executorService.shutdown();
      }
    }

    log.info("Output scheduler shutting down now");

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
      return decoratedProcessor.processAsync(context, taskRun.getExecutorService());
    } catch (Exception exception) {
      if (gracefulShutdownOnError) {
        log.error(
            "Error while preparing/processing processor '{}'. Shutting down gracefully",
            nextProcessor.getClass().getSimpleName(),
            exception
        );
        gracefulShutdown();
        return CompletableFuture.completedFuture(null);
      }

      throw exception;
    }
  }
}
