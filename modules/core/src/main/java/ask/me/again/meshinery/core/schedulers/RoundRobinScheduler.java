package ask.me.again.meshinery.core.schedulers;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.TaskRun;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class RoundRobinScheduler<K, C extends Context> {

  private final ConcurrentLinkedQueue<TaskRun<C>> todoQueue = new ConcurrentLinkedQueue<>();
  private final List<MeshineryTask<K, C>> tasks;

  @SneakyThrows
  public void start(AtomicBoolean atomicBoolean) {
    //the producer
    createScheduler(atomicBoolean);

    //the worker
    Executors.newSingleThreadExecutor().execute(() -> run(atomicBoolean));
  }

  private void createScheduler(AtomicBoolean atomicBoolean) {
    Executors.newSingleThreadExecutor().execute(() -> {
      while (atomicBoolean.get()) {
        for (var reactiveTask : tasks) {
          var inputList = reactiveTask.getInputSource().getInputs(reactiveTask.getInputKey());

          var executorService = reactiveTask.getExecutorService();

          for (var input : inputList) {
            var processorQueue = new LinkedList<>(reactiveTask.getProcessorList());
            var taskRun = new TaskRun<>(CompletableFuture.completedFuture(input), processorQueue, executorService);
            todoQueue.add(taskRun);
          }
        }
      }
    });
  }

  @SneakyThrows
  public void run(AtomicBoolean atomicBoolean) {

    System.out.println("Thread started");

    while (atomicBoolean.get() || !todoQueue.isEmpty()) {
      var currentTask = todoQueue.poll();

      if (currentTask == null) {
        Thread.sleep(500);
        continue;
      }

      if (currentTask.getFuture().isDone()) {

        if (currentTask.getQueue().isEmpty()) {
          continue;
        }

        var nextProcessor = currentTask.getQueue().remove();
        C context = currentTask.getFuture().get();

        if (context == null) {
          continue;
        }

        currentTask.setFuture(nextProcessor.processAsync(context, currentTask.getExecutorService()));
      }

      todoQueue.add(currentTask);
    }
  }
}
