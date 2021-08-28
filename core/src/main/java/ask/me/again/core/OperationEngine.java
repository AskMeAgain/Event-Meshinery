package ask.me.again.core;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OperationEngine<C extends Context> implements Runnable {

  private final ConcurrentLinkedQueue<TaskRun<C>> todoQueue = new ConcurrentLinkedQueue<>();

  public OperationEngine(List<ReactiveTask<C>> tasks) throws InterruptedException {
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

    Thread.sleep(1000);
    //the worker
    Executors.newSingleThreadExecutor().execute(this);

  }


  @Override
  @SneakyThrows
  public void run() {

    System.out.println("Thread started");

    while (!todoQueue.isEmpty()) {
      var workingOn = todoQueue.remove();

      if (workingOn.future.isDone()) {

        if (workingOn.queue.isEmpty()) {
          continue;
        }

        var nextProcessor = workingOn.queue.remove();
        C context = workingOn.future.get();
        System.out.println("Received id: " + context.getId());
        workingOn.setFuture(nextProcessor.processAsync(context));
      }

      todoQueue.add(workingOn);
    }
  }

  private C getInputFromKafkaTopic(String id) {
    return (C) TestContext.builder()
      .testValue1(10)
      .id(id)
      .build();
  }

}
