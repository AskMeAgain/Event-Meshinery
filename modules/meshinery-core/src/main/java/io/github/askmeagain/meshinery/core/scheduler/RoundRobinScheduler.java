package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.task.TaskDataProperties;
import io.github.askmeagain.meshinery.core.task.TaskRun;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
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
import java.util.function.Consumer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@Getter
@SuppressWarnings("checkstyle:MissingJavadocType")
public class RoundRobinScheduler {

  private final List<MeshineryTask<?, ?>> tasks;
  private final int backpressureLimit;
  private final boolean isBatchJob;
  private final List<? extends Consumer<RoundRobinScheduler>> shutdownHook;
  private final List<? extends Consumer<RoundRobinScheduler>> startupHook;
  private final List<? extends Consumer<MeshineryDataContext>> listPreTaskRunHook;
  private final List<? extends Consumer<MeshineryDataContext>> listPostTaskRunHook;

  private final List<ProcessorDecorator<MeshineryDataContext, MeshineryDataContext>> processorDecorator;
  private final boolean gracefulShutdownOnError;
  private final int gracePeriodMilliseconds;

  private final Queue<TaskRun> outputQueue = new ConcurrentLinkedQueue<>();
  private final Queue<ConnectorKey> inputQueue = new ConcurrentLinkedQueue<>();

  private final Map<ConnectorKey, MeshineryTask<?, ?>> taskRunLookupMap = new ConcurrentHashMap<>();
  private final ExecutorService taskExecutorService;
  private final Set<DataInjectingExecutorService> executorServices = new HashSet<>();
  private final DataInjectingExecutorService taskExecutor;
  private final DataInjectingExecutorService inputExecutor;
  private final AtomicBoolean gracefulShutdownTriggered = new AtomicBoolean();
  private Instant lastInputEntry;
  private final Set<String> currentTasks = new HashSet<>();
  private final Map<Integer, List<Integer>> mapIntegerListInteger = new ConcurrentHashMap<>();

  @SneakyThrows
  RoundRobinScheduler(
      List<MeshineryTask<?, ?>> tasks,
      int backpressureLimit,
      boolean isBatchJob,
      List<? extends Consumer<RoundRobinScheduler>> shutdownHook,
      List<? extends Consumer<RoundRobinScheduler>> startupHook,
      List<? extends Consumer<MeshineryDataContext>> preTaskRunHook,
      List<? extends Consumer<MeshineryDataContext>> postTaskRunHook,
      List<ProcessorDecorator<MeshineryDataContext, MeshineryDataContext>> processorDecorator,
      boolean gracefulShutdownOnError,
      int gracePeriodMilliseconds,
      DataInjectingExecutorService taskExecutorService
  ) {
    this.tasks = tasks;
    this.listPostTaskRunHook = postTaskRunHook;
    this.listPreTaskRunHook = preTaskRunHook;
    this.backpressureLimit = backpressureLimit;
    this.isBatchJob = isBatchJob;
    this.shutdownHook = shutdownHook;
    this.startupHook = startupHook;
    this.processorDecorator = processorDecorator;
    this.gracefulShutdownOnError = gracefulShutdownOnError;
    this.gracePeriodMilliseconds = gracePeriodMilliseconds;
    this.taskExecutorService = taskExecutorService;

    //setup
    createLookupMap();
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

  private void createLookupMap() {
    for (var task : tasks) {
      var connectorKey = ConnectorKey.builder()
          .connector((MeshineryInputSource<Object, MeshineryDataContext>) task.getInputConnector())
          .key(task.getInputKeys())
          .build();

      taskRunLookupMap.put(connectorKey, task);
    }
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

    var queuesHaveWorkTodo = ((!outputQueue.isEmpty() || !inputQueue.isEmpty()));

    while (!executor.isShutdown() && !gracefulShutdownTriggered.get() && queuesHaveWorkTodo) {
      if (backpressureLimit <= outputQueue.size()) {
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
    Thread.currentThread().setName("meshinery-output");
    log.info("Starting processing worker thread");

    while (!executor.isShutdown()) {

      var taskRun = outputQueue.poll();

      if (taskRun == null) {
        if (gracefulShutdownTriggered.get()) {
          //we shutdown in case we had a graceful shutdown
          break;
        }
        continue;
      }

      getResultFuture(taskRun);
    }

    for (var executorService : executorServices) {
      executorService.shutdown();
    }

    log.info("Output scheduler shutting down now");

    shutdownHook.forEach(hook -> hook.accept(this));
  }

  private void getResultFuture(TaskRun run) {
    CompletableFuture.runAsync(() -> {
      var contextId = run.getContext().getId();
      try {
        currentTasks.add(contextId);
        MDC.put(TaskDataProperties.TASK_NAME, run.getTaskName());
        MDC.put(TaskDataProperties.TASK_ID, contextId);
        TaskData.setTaskData(run.getTaskData());

        listPreTaskRunHook.forEach(con -> con.accept(run.getContext()));

        var context = run.getContext();
        while (!run.getQueue().isEmpty()) {
          var processor = run.getQueue().remove();
          var decoratedProc = MeshineryUtils.applyDecorators(processor, processorDecorator);

          try {
            context = decoratedProc.processAsync(context);
          } catch (Exception e) {
            context = run.getHandleError().apply(context, e);
          }
        }

        listPostTaskRunHook.forEach(con -> con.accept(run.getContext()));

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
}
