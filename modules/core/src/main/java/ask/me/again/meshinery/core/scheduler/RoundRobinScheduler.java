package ask.me.again.meshinery.core.scheduler;

import ask.me.again.meshinery.core.common.DataContext;
import ask.me.again.meshinery.core.other.DataInjectingExecutorService;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.ProcessorDecorator;
import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.core.task.MeshineryTaskVerifier;
import ask.me.again.meshinery.core.task.TaskData;
import ask.me.again.meshinery.core.task.TaskRun;
import java.util.Collections;
import java.util.List;
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

import static ask.me.again.meshinery.core.other.MeshineryUtils.applyDecorators;
import static ask.me.again.meshinery.core.task.TaskDataProperties.TASK_NAME;
import static ask.me.again.meshinery.core.task.TaskDataProperties.UID;

@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.MODULE)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class RoundRobinScheduler {

  private final List<MeshineryTask<?, ?>> tasks;
  private final List<ExecutorService> executorServices;
  private final ConcurrentLinkedQueue<TaskRun> todoQueue;
  private final int backpressureLimit;
  private final boolean isBatchJob;
  private final List<? extends Consumer<RoundRobinScheduler>> shutdownHook;
  private final List<? extends Consumer<RoundRobinScheduler>> startupHook;
  private final List<ProcessorDecorator<DataContext, DataContext>> processorDecorator;
  private boolean internalShutdown = false;

  public static SchedulerBuilder builder() {
    return new SchedulerBuilder();
  }

  @SneakyThrows
  RoundRobinScheduler start() {
    MeshineryTaskVerifier.verifyTasks(tasks);

    //task gathering
    tasks.forEach(task -> executorServices.add(task.getExecutorService()));

    //the producer
    var inputExecutor = Executors.newSingleThreadExecutor();
    executorServices.add(inputExecutor);
    createInputScheduler(inputExecutor);

    Thread.sleep(100);

    //the worker
    var taskExecutor = Executors.newSingleThreadExecutor();
    executorServices.add(taskExecutor);
    taskExecutor.execute(this::runWorker);

    startupHook.forEach(hook -> hook.accept(this));

    return this;
  }

  public void gracefulShutdown() {
    log.info("Graceful shutdown");
    internalShutdown = true;
  }

  private void createInputScheduler(ExecutorService executor) {
    executor.execute(() -> {
      log.info("Starting input worker thread");
      newInputIteration:
      while (!executor.isShutdown() && !internalShutdown) {

        var itemsInThisIteration = 0;
        if (todoQueue.size() < backpressureLimit) {

          for (var reactiveTask : tasks) {
            //getting the input values
            MDC.put(TASK_NAME, reactiveTask.getTaskName());

            var taskRuns = queryTaskRuns(reactiveTask);

            if (taskRuns.size() > 0) {
              log.debug("Received data from input source: {}", reactiveTask.getInputSource().getName());
            }

            for (var taskRun : taskRuns) {
              itemsInThisIteration++;

              todoQueue.add(taskRun);

              //checking backpressure
              if (todoQueue.size() >= backpressureLimit) {
                continue newInputIteration;
              }
            }
          }
        } else {
          itemsInThisIteration = -1; //so we dont finish the batch job because of backpressure
        }

        //we did not add any work in a single iteration. We are done
        if (itemsInThisIteration == 0 && isBatchJob) {
          log.info("Shutdown through batch job flag");
          gracefulShutdown();
          break;
        }
        //shutdown already triggered, we just stop
        if (internalShutdown) {
          break;
        }
      }
      MDC.clear();
      log.info("Input scheduler gracefully shutdown");
    });
  }

  private List<TaskRun> queryTaskRuns(MeshineryTask<?, ? extends DataContext> reactiveTask) {
    try {
      return reactiveTask.getNewTaskRuns();
    } catch (Exception e) {
      log.error("Error while requesting new input data. Shutting down scheduler", e);
      gracefulShutdown();
      return Collections.emptyList();
    }
  }

  @SneakyThrows
  private void runWorker() {

    log.info("Starting processing worker thread");

    //we use this label to break out of the task in case we dont want to work on it
    newTask:
    while (!internalShutdown || !todoQueue.isEmpty()) {
      var currentTask = todoQueue.poll();

      if (currentTask == null) {
        Thread.sleep(500);
        continue;
      }

      MDC.put(TASK_NAME, currentTask.getTaskName());
      MDC.put(UID, currentTask.getId());

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
      var decoratedProcessor = applyDecorators(nextProcessor, processorDecorator);
      return decoratedProcessor.processAsync(context, executorService);
    } catch (Exception e) {
      log.error(
          "Error while preparing/processing processor '{}'. Shutting down gracefully",
          nextProcessor.getClass().getSimpleName(),
          e
      );
      gracefulShutdown();
      return CompletableFuture.completedFuture(null);
    }
  }
}
