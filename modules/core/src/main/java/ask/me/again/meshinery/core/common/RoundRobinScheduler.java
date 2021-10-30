package ask.me.again.meshinery.core.common;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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

  public void gracefulShutdown() {
    System.out.println("Graceful shutdown");
    internalShutdown = true;
  }

  @SneakyThrows
  private void run() {

    System.out.println("Thread started");

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

    System.out.println("Reached end of Queue");

    for (var executorService : executorServices) {
      if (!executorService.isShutdown()) {
        executorService.shutdown();
      }
    }
  }

  private void createInputScheduler(ExecutorService executor) {
    executor.execute(() -> {
      while (!executor.isShutdown()) {

        if (todoQueue.size() > backpressureLimit) {
          continue;
        }

        var counter = 0;
        for (var reactiveTask : tasks) {
          //getting the input values
          var inputList = reactiveTask.getInputValues();
          var executorService = reactiveTask.getExecutorService();

          for (var input : inputList) {
            counter++;
            var processorQueue = new LinkedList<>(reactiveTask.getProcessorList());
            var taskRun = new TaskRun(CompletableFuture.completedFuture(input), processorQueue, executorService);
            todoQueue.add(taskRun);
          }
        }

        //we did not add any work in a single iteration. We are done
        if (counter == 0 && isBatchJob) {
          System.out.println("Shutdown through batch job flag");
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

  @SneakyThrows
  RoundRobinScheduler start() {

    //task gathering
    tasks.forEach(task -> executorServices.add(task.getExecutorService()));

    //the producer
    var inputExecutor = Executors.newSingleThreadExecutor();
    executorServices.add(inputExecutor);
    createInputScheduler(inputExecutor);

    //the worker
    var taskExecutor = Executors.newSingleThreadExecutor();
    executorServices.add(taskExecutor);
    taskExecutor.execute(this::run);

    return this;
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

    public Builder tasks(List<MeshineryTask<?, ? extends Context>> tasks) {
      tasks.addAll(tasks);
      return this;
    }

    public Builder isBatchJob(boolean flag) {
      isBatchJob = flag;
      return this;
    }

    @SneakyThrows
    public RoundRobinScheduler build() {
      return new RoundRobinScheduler(tasks, executorServices, todoQueue, backpressureLimit, isBatchJob).start();
    }
  }
}
