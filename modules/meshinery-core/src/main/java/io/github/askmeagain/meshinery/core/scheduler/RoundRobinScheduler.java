package io.github.askmeagain.meshinery.core.scheduler;

import com.cronutils.utils.VisibleForTesting;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.task.TaskDataProperties;
import io.github.askmeagain.meshinery.core.task.TaskRun;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@Getter
@SuppressWarnings("checkstyle:MissingJavadocType")
public class RoundRobinScheduler {

  private Instant lastInputEntry;
  private final boolean isBatchJob;
  private final int backpressureLimit;
  private final List<MeshineryTask> tasks;
  private final int gracePeriodMilliseconds;
  private final boolean gracefulShutdownOnError;
  private final ExecutorService taskExecutorService;
  private final DataInjectingExecutorService taskExecutor;
  private final DataInjectingExecutorService inputExecutor;
  private final List<? extends Consumer<RoundRobinScheduler>> startupHook;
  private final List<? extends Consumer<RoundRobinScheduler>> shutdownHook;

  private final Set<String> currentTasks = new HashSet<>();
  private final Queue<TaskRun> outputQueue = new ConcurrentLinkedQueue<>();
  private final AtomicBoolean gracefulShutdownTriggered = new AtomicBoolean();
  private final Queue<ConnectorKey> inputQueue = new ConcurrentLinkedQueue<>();
  private final Set<DataInjectingExecutorService> executorServices = new HashSet<>();
  private final Map<ConnectorKey, MeshineryTask> taskRunLookupMap = new ConcurrentHashMap<>();
  private final Map<Integer, List<Integer>> mapIntegerListInteger = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Instant> lastExecutions = new ConcurrentHashMap<>();

  RoundRobinScheduler(
      List<MeshineryTask> tasks,
      int backpressureLimit,
      boolean isBatchJob,
      List<? extends Consumer<RoundRobinScheduler>> shutdownHook,
      List<? extends Consumer<RoundRobinScheduler>> startupHook,
      boolean gracefulShutdownOnError,
      int gracePeriodMilliseconds,
      DataInjectingExecutorService taskExecutorService
  ) {
    this.tasks = tasks;
    this.backpressureLimit = backpressureLimit;
    this.isBatchJob = isBatchJob;
    this.shutdownHook = shutdownHook;
    this.startupHook = startupHook;
    this.gracefulShutdownOnError = gracefulShutdownOnError;
    this.gracePeriodMilliseconds = gracePeriodMilliseconds;
    this.taskExecutorService = taskExecutorService;

    //setup
    createTaskRunLookupMap();
    executorServices.add(taskExecutorService);

    //the producer
    inputExecutor = new DataInjectingExecutorService("input-executor", Executors.newSingleThreadExecutor());
    executorServices.add(inputExecutor);

    //the worker
    taskExecutor = new DataInjectingExecutorService("output-executor", Executors.newSingleThreadExecutor());
    executorServices.add(taskExecutor);
  }

  public static RoundRobinSchedulerBuilder builder() {
    return new RoundRobinSchedulerBuilder();
  }

  @SneakyThrows
  public RoundRobinScheduler start() {
    startupHook.forEach(hook -> hook.accept(this));
    inputExecutor.execute(() -> createInputScheduler(inputExecutor));
    Thread.sleep(100);
    taskExecutor.execute(() -> runWorker(taskExecutor));
    return this;
  }

  public void gracefulShutdown() {
    log.info("Graceful shutdown triggered. Shutting down all threads");
    gracefulShutdownTriggered.set(true);
  }

  @SneakyThrows
  private void createInputScheduler(ExecutorService executor) {
    Thread.currentThread().setName("meshinery-input");
    log.info("Starting input worker thread");

    inputQueue.addAll(fillQueueFromTasks());
    lastInputEntry = Instant.now();

    while (!executor.isShutdown() && !gracefulShutdownTriggered.get()) {
      if (backpressureLimit <= outputQueue.size()) {
        log.info("Waiting because of backpressure");
        Thread.sleep(1000);
        continue;
      }

      while (!inputQueue.isEmpty()) {
        var work = inputQueue.peek();
        var newTaskRuns = queryTaskRuns(work);
        outputQueue.addAll(newTaskRuns); //in this order so any queue is always filled
        inputQueue.remove();
      }

      if (isBatchJob) {
        if (outputQueue.isEmpty()) {
          if (lastInputEntry.plusMillis(gracePeriodMilliseconds).isBefore(Instant.now())) {
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
        .map(task -> ConnectorKey.builder()
            .connector(task.getInputConnector())
            .key(task.getInputKeys())
            .build())
        .toList();
  }

  private List<TaskRun> queryTaskRuns(ConnectorKey work) {
    try {
      if (!taskRunLookupMap.containsKey(work)) {
        return Collections.emptyList();
      }

      return getNewTaskRuns(taskRunLookupMap.get(work));
    } catch (Exception e) {
      if (gracefulShutdownOnError) {
        log.error("Error while requesting new input data. Shutting down scheduler", e);
        gracefulShutdown();
        return Collections.emptyList();
      }
      throw e;
    }
  }

  /**
   * Pulls the next batch of data from the input source. Keeps the backoff period in mind, which in this case returns
   * empty list and doesnt poll the source
   *
   * @return returns TaskRuns
   */
  @VisibleForTesting
  List<TaskRun> getNewTaskRuns(MeshineryTask<Object, ? extends MeshineryDataContext> task) {
    var nextExecution = lastExecutions.computeIfAbsent(task.getTaskName(), k -> Instant.now());
    var now = Instant.now();

    if (!now.isAfter(nextExecution)) {
      return Collections.emptyList();
    }

    try {
      TaskData.setTaskData(task.getTaskData());
      lastExecutions.put(task.getTaskName(), now.plusMillis(task.getBackoffTimeMilli()));
      return task.getInputConnector()
          .getInputs(task.getInputKeys())
          .stream()
          .map(input -> {
            var processorList = task.getProcessorList();
            var queue = new LinkedList<MeshineryProcessor>(processorList);
            return TaskRun.builder()
                .taskName(task.getTaskName())
                .taskData(task.getTaskData())
                .context(input)
                .handleError(
                    (BiFunction<MeshineryDataContext, Throwable, MeshineryDataContext>) task.getHandleException())
                .queue(queue)
                .build();
          })
          .toList();
    } finally {
      TaskData.clearTaskData();
    }
  }

  private void runWorker(ExecutorService executor) {
    Thread.currentThread().setName("meshinery-output");
    log.info("Starting processing worker thread");

    while (!executor.isShutdown()) {
      var taskRun = outputQueue.poll();
      if (taskRun == null) {
        if (gracefulShutdownTriggered.get()) {
          break; //we shutdown in case we had a graceful shutdown
        }
        continue;
      }
      scheduleTaskRunOnExecutor(taskRun);
    }

    for (var executorService : executorServices) {
      executorService.shutdown();
    }

    log.info("Output scheduler shutting down now");

    shutdownHook.forEach(hook -> hook.accept(this));
  }

  private void scheduleTaskRunOnExecutor(TaskRun run) {
    CompletableFuture.runAsync(() -> {
      var contextId = run.getContext().getId();
      try {
        currentTasks.add(contextId);
        MDC.put(TaskDataProperties.TASK_NAME, run.getTaskName());
        MDC.put(TaskDataProperties.TASK_ID, contextId);
        TaskData.setTaskData(run.getTaskData());

        var context = run.getContext();
        while (!run.getQueue().isEmpty()) {
          try {
            var processor = run.getQueue().remove();
            if (context == null) {
              break;
            }
            context = processor.process(context);
          } catch (Exception e) {
            context = run.getHandleError().apply(context, e);
          }
        }

      } catch (Exception exception) {
        if (gracefulShutdownOnError) {
          log.error("Error while processing. Shutting down gracefully", exception);
          gracefulShutdown();
        }
        log.error("Processor completed with error", exception);
        throw exception;
      } finally {
        currentTasks.remove(contextId);
        TaskData.clearTaskData();
        MDC.clear();
      }
    }, taskExecutorService);
  }

  private void createTaskRunLookupMap() {
    for (var task : tasks) {
      var connectorKey = ConnectorKey.builder()
          .connector(task.getInputConnector())
          .key(task.getInputKeys())
          .build();

      taskRunLookupMap.put(connectorKey, task);
    }
  }
}
