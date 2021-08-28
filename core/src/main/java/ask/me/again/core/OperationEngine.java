package ask.me.again.core;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class OperationEngine<C extends Context> implements Runnable {

  private final List<ReactiveProcessor<C>> reactiveProcessors;

  public static <C extends Context> void construct(String name, List<ReactiveProcessor<C>> operations) {
    new Thread(new OperationEngine<C>(operations), name).start();
  }

  @Override
  @SneakyThrows
  public void run() {

    System.out.println("Thread started");

    Queue<TaskRun<C>> todoQueue = new LinkedList<>();

    todoQueue.add(new TaskRun<>(CompletableFuture.completedFuture(getInputFromKafkaTopic("A")), new LinkedList<>(reactiveProcessors)));
    todoQueue.add(new TaskRun<>(CompletableFuture.completedFuture(getInputFromKafkaTopic("B")), new LinkedList<>(reactiveProcessors)));
    todoQueue.add(new TaskRun<>(CompletableFuture.completedFuture(getInputFromKafkaTopic("C")), new LinkedList<>(reactiveProcessors)));

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

  @RequiredArgsConstructor
  public static class TaskRun<C extends Context> {

    @Setter
    CompletableFuture<C> future;
    Queue<ReactiveProcessor<C>> queue;

    public TaskRun(CompletableFuture<C> future, Queue<ReactiveProcessor<C>> queue) {
      this.future = future;
      this.queue = queue;
    }
  }
}
