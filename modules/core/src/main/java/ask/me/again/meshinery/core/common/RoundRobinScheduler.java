package ask.me.again.meshinery.core.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class RoundRobinScheduler {

  private final List<MeshineryTask<?, ? extends Context>> tasks;
  private final List<ExecutorService> executorServices;
  private final ConcurrentLinkedQueue<TaskRun> todoQueue;
  private final int backpressureLimit;
  private final boolean isBatchJob;

  private boolean internalShutdown = false;

  public static RoundRobinScheduler.Builder builder() {
    return new RoundRobinScheduler.Builder();
  }

  @SneakyThrows
  private RoundRobinScheduler start() {
    log.info("Starting Scheduler with following Tasks: {}", getAndVerifyTaskList());
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

    return this;
  }

  private List<String> getAndVerifyTaskList() {
    var result = tasks.stream()
        .map(MeshineryTask::getTaskName)
        .toList();

    var duplicates = findDuplicates(result);

    if (duplicates.size() > 0) {
      throw new RuntimeException("Found duplicate job names: [" + String.join(", ", duplicates) + "]");
    }

    return result;
  }

  //https://stackoverflow.com/a/31641116/5563263
  private <T> Set<T> findDuplicates(Collection<T> collection) {
    Set<T> uniques = new HashSet<>();
    return collection.stream()
        .filter(e -> !uniques.add(e))
        .collect(Collectors.toSet());
  }

  public void gracefulShutdown() {
    log.info("Graceful shutdown");
    internalShutdown = true;
  }

  private void createInputScheduler(ExecutorService executor) {
    executor.execute(() -> {
      log.info("Starting input worker thread");
      newInputIteration:
      while (!executor.isShutdown()) {

        var itemsInThisIteration = 0;
        if (todoQueue.size() < backpressureLimit) {

          for (var reactiveTask : tasks) {
            //getting the input values
            MDC.put("taskid", reactiveTask.getTaskName());

            var inputList = requestNewData(reactiveTask);

            var executorService = reactiveTask.getExecutorService();

            for (var input : inputList) {
              itemsInThisIteration++;
              var processorQueue = new LinkedList<>(reactiveTask.getProcessorList());
              var taskRun = TaskRun.builder()
                  .taskName(reactiveTask.getTaskName())
                  .id(input.getId())
                  .future(CompletableFuture.completedFuture(input))
                  .executorService(executorService)
                  .queue(processorQueue)
                  .handleError(reactiveTask.getHandleException())
                  .build();

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
    });
  }

  private List<? extends Context> requestNewData(MeshineryTask<?, ? extends Context> reactiveTask) {
    try {
      return reactiveTask.getInputValues();
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

      MDC.put("taskid", currentTask.getTaskName());
      MDC.put("uid", currentTask.getId());

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

        var resultFuture = getResultFuture(nextProcessor, context, executorService);
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
  }

  private CompletableFuture<Context> getResultFuture(
      MeshineryProcessor<Context, Context> nextProcessor, Context context, MdcInjectingExecutorService executorService
  ) {
    try {
      return nextProcessor.processAsync(context, executorService);
    } catch (Exception e) {
      log.error(
          "Error while preparing/processing processor '{}'. Shutting down gracefully",
          nextProcessor.getClass().getSimpleName()
      );
      gracefulShutdown();
      return CompletableFuture.completedFuture(null);
    }
  }

  public static class Builder {

    int backpressureLimit = 200;
    boolean isBatchJob;
    List<MeshineryTask<? extends Object, ? extends Context>> tasks = new ArrayList<>();
    List<ExecutorService> executorServices = new ArrayList<>();
    ConcurrentLinkedQueue<TaskRun> todoQueue = new ConcurrentLinkedQueue<>();

    public Builder task(MeshineryTask<?, ? extends Context> task) {
      tasks.add(task);
      return this;
    }

    public Builder backpressureLimit(int backpressureLimit) {
      this.backpressureLimit = backpressureLimit;
      return this;
    }

    public Builder tasks(List<MeshineryTask<?, ? extends Context>> task) {
      tasks.addAll(task);
      return this;
    }

    public Builder isBatchJob(boolean flag) {
      isBatchJob = flag;
      return this;
    }

    @SneakyThrows
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    public RoundRobinScheduler build() {
      return new RoundRobinScheduler(
          tasks,
          executorServices,
          todoQueue,
          backpressureLimit,
          isBatchJob
      ).start();
    }
  }
}
