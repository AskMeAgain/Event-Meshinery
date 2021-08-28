package ask.me.again.core;

import lombok.SneakyThrows;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;

public class WorkerService<C extends Context> implements Runnable {

  private final ConcurrentLinkedQueue<TaskRun<C>> todoQueue = new ConcurrentLinkedQueue<>();

  public WorkerService(List<ReactiveTask<C>> tasks) {
    //the producer
    Executors.newSingleThreadExecutor().execute(() -> {
      for (int i = 0; i < 10; i++) {
        var input = (C) TestContext.builder()
          .testValue1(i)
          .id(i + "")
          .build();
        todoQueue.add(new TaskRun<C>(CompletableFuture.completedFuture(input), new LinkedList<>(tasks.get(i % tasks.size()).getProcessorList())));
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });

    //the worker
    Executors.newSingleThreadExecutor().execute(this);
  }

  @Override
  @SneakyThrows
  public void run() {

    System.out.println("Thread started");

    while (!todoQueue.isEmpty()) {
      var currentTask = todoQueue.remove();

      if (currentTask.future.isDone()) {

        if (currentTask.queue.isEmpty()) {
          continue;
        }

        var nextProcessor = currentTask.queue.remove();
        C context = currentTask.future.get();

        currentTask.setFuture(nextProcessor.processAsync(context));
      }

      todoQueue.add(currentTask);
    }
  }
}
