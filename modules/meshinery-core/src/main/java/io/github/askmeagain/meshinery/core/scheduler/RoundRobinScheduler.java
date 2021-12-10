package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskVerifier;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.task.TaskDataProperties;
import io.github.askmeagain.meshinery.core.task.TaskRun;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

  public static final int INT = 20;
  private final List<MeshineryTask<?, ?>> tasks;
  private final ConcurrentLinkedQueue<TaskRun> todoQueue;
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

    //the producer
    var inputExecutor = new DataInjectingExecutorService("input-executor", Executors.newSingleThreadExecutor());
    executorServices.add(inputExecutor);
    inputExecutor.execute(() -> createInputScheduler(inputExecutor));

    //the worker
    var taskExecutor = new DataInjectingExecutorService("output-executor", Executors.newSingleThreadExecutor());
    executorServices.add(taskExecutor);
    taskExecutor.execute(() -> runWorker(taskExecutor));

    startupHook.forEach(hook -> hook.accept(this));

    return this;
  }

  public void gracefulShutdown() {
    log.info("Graceful shutdown");
    internalShutdown = true;
  }

  @SneakyThrows
  private void createInputScheduler(ExecutorService executor) {
    log.info("Starting input worker thread");
    newInputIteration:
    while (!executor.isShutdown() && !internalShutdown) {

      var emptyTaskRun = true;
      for (var reactiveTask : tasks) {
        //getting the input values
        MDC.put(TaskDataProperties.TASK_NAME, reactiveTask.getTaskName());

        var taskRuns = queryTaskRuns(reactiveTask);
        todoQueue.addAll(taskRuns);

        if (!taskRuns.isEmpty()) {
          emptyTaskRun = false;
        }

        //checking backpressure
        if (todoQueue.size() >= backpressureLimit) {
          continue newInputIteration;
        }
      }

      //shutdown already triggered, we just stop
      if (internalShutdown) {
        break;
      }

      if (emptyTaskRun) {
        Thread.sleep(500);
        inputDone.incrementAndGet();
      } else {
        inputDone.set(0);
        outputDone.set(0);
      }

      if (inputDone.get() > INT && outputDone.get() > INT) {
        break;
      }
    }

    MDC.clear();
    log.info("Input scheduler gracefully shutdown");
  }

  private List<TaskRun> queryTaskRuns(MeshineryTask<?, ? extends DataContext> reactiveTask) {
    try {
      return reactiveTask.getNewTaskRuns();
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

    log.info("Starting processing worker thread");

    //we use this label to break out of the task in case we dont want to work on it
    newTask:
    while (!internalShutdown && !executor.isShutdown()) {

      var currentTask = todoQueue.poll();
      if (currentTask == null) {
        Thread.sleep(500);
        outputDone.incrementAndGet();
        if (outputDone.get() > INT && inputDone.get() > INT) {
          break;
        }
        continue;
      }
      inputDone.set(0);
      outputDone.set(0);

      MDC.put(TaskDataProperties.TASK_NAME, currentTask.getTaskName());
      MDC.put(TaskDataProperties.UID, currentTask.getId());

      while (currentTask.getFuture().isDone()) {
        var queue = currentTask.getQueue();

        //we stop if we reached the end of the queue
        if (queue.isEmpty()) {
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
          continue newTask;
        }

        var executorService = currentTask.getExecutorService();

        var resultFuture = getResultFuture(currentTask.getTaskData(), nextProcessor, context, executorService);
        currentTask = currentTask.withFuture(resultFuture);
      }

      MDC.clear();

      todoQueue.add(currentTask);
    }

    log.info("Reached end of Queue. Shutting down now");

    for (var executorService : executorServices) {
      if (!executorService.isShutdown()) {
        executorService.shutdown();
      }
    }

    shutdownHook.forEach(hook -> hook.accept(this));
  }

  private CompletableFuture<DataContext> getResultFuture(
      TaskData taskData,
      MeshineryProcessor<DataContext, DataContext> nextProcessor,
      DataContext context,
      DataInjectingExecutorService executorService
  ) {
    try {
      TaskData.setTaskData(taskData);
      var decoratedProcessor = MeshineryUtils.applyDecorators(nextProcessor, processorDecorator);
      return decoratedProcessor.processAsync(context, executorService);
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
