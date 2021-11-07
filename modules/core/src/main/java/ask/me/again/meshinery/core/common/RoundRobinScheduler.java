package ask.me.again.meshinery.core.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
  RoundRobinScheduler start() {
    log.info("Starting Scheduler with the following Tasks: {}", tasks.stream().map(MeshineryTask::getTaskName).toList());
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

  public void gracefulShutdown() {
    log.info("Graceful shutdown");
    internalShutdown = true;
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

      while (currentTask.getFuture().isDone()) {
        //we stop if we reached the end of the queue
        if (currentTask.getQueue().isEmpty()) {
          continue newTask;
        }

        var nextProcessor = currentTask.getQueue().remove();
        var context = currentTask.getFuture().get();

        //we stop if the context is null
        if (context == null) {
          continue newTask;
        }

        currentTask.setFuture(nextProcessor.processAsync(context, currentTask.getExecutorService()));
      }

      todoQueue.add(currentTask);
    }

    log.info("Reached end of Queue. Shutting down now");

    for (var executorService : executorServices) {
      if (!executorService.isShutdown()) {
        executorService.shutdown();
      }
    }
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
            var inputList = reactiveTask.getInputValues();
            var executorService = reactiveTask.getExecutorService();

            for (var input : inputList) {
              itemsInThisIteration++;
              var processorQueue = new LinkedList<>(reactiveTask.getProcessorList());
              var taskRun = new TaskRun(CompletableFuture.completedFuture(input), processorQueue, executorService);
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
    });
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
