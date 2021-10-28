package ask.me.again.meshinery.core.schedulers;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.TaskRun;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class RoundRobinScheduler<K, O extends Context> {

  private final boolean isBatchJob;
  private final List<MeshineryTask<K, O>> tasks;
  private final List<ExecutorService> executorServices = new ArrayList<>();
  private final ConcurrentLinkedQueue<TaskRun> todoQueue = new ConcurrentLinkedQueue<>();

  private boolean internalShutdown;

  @SneakyThrows
  public void start() {

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

  }

  public void gracefulShutdown() {
    System.out.println("Graceful shutdown");
    internalShutdown = true;
  }

  @SneakyThrows
  public void run() {

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

        var counter = 0;
        for (var reactiveTask : tasks) {
          var inputList = reactiveTask.getInputSource().getInputs(reactiveTask.getInputKey());

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
}
