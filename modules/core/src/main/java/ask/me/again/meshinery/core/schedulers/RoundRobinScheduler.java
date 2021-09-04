package ask.me.again.meshinery.core.schedulers;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.TaskRun;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
public class RoundRobinScheduler<K, C extends Context> {

  private final ConcurrentLinkedQueue<TaskRun<C>> todoQueue = new ConcurrentLinkedQueue<>();
  private final List<MeshineryTask<K, C>> tasks;
  private final boolean isBatchJob;
  private final List<ExecutorService> executorServices = new ArrayList<>();

  @SneakyThrows
  public void start() {

    //task gathering
    tasks.forEach(task -> executorServices.add(task.getExecutorService()));

    //the producer
    var inputExecutor = Executors.newSingleThreadExecutor();
    executorServices.add(inputExecutor);
    createScheduler(inputExecutor);

    //the worker
    var taskExecutor = Executors.newSingleThreadExecutor();
    executorServices.add(taskExecutor);
    taskExecutor.execute(this::run);

  }

  private void createScheduler(ExecutorService executor) {
    executor.execute(() -> {
      while (!executor.isShutdown()) {

        var counter = 0;
        for (var reactiveTask : tasks) {
          var inputList = reactiveTask.getInputSource().getInputs(reactiveTask.getInputKey());

          var executorService = reactiveTask.getExecutorService();

          for (var input : inputList) {
            counter++;
            var processorQueue = new LinkedList<>(reactiveTask.getProcessorList());
            var taskRun = new TaskRun<>(CompletableFuture.completedFuture(input), processorQueue, executorService);
            todoQueue.add(taskRun);
          }
        }

        //we did not add any work in a single iteration. We are done
        if (counter == 0 && isBatchJob) {
          break;
        }
      }

      shutdown();
    });
  }

  public void shutdown() {
    for (var executorService : executorServices) {
      if (!executorService.isShutdown()) {
        executorService.shutdown();
      }
    }
  }

  @SneakyThrows
  public void run() {

    System.out.println("Thread started");

    //we use this label to break out of the task in case we dont want to work on it
    currentTask:
    while (!todoQueue.isEmpty() || !isBatchJob) {
      var currentTask = todoQueue.poll();

      if (currentTask == null) {
        Thread.sleep(500);
        continue;
      }

      while (currentTask.getFuture().isDone()) {

        //we stop if we reached the end of the queue or shutting down executors
        if (currentTask.getQueue().isEmpty() || currentTask.getExecutorService().isShutdown()) {
          continue currentTask;
        }

        var nextProcessor = currentTask.getQueue().remove();
        C context = currentTask.getFuture().get();

        //we stop the task if the context is null
        if (context == null) {
          continue currentTask;
        }

        currentTask.setFuture(nextProcessor.processAsync(context, currentTask.getExecutorService()));
      }

      todoQueue.add(currentTask);
    }
  }

}
