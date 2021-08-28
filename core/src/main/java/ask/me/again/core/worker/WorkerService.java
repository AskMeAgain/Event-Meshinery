package ask.me.again.core.worker;

import ask.me.again.core.common.Context;
import ask.me.again.core.common.TaskRun;
import ask.me.again.core.example.TestContext;
import lombok.SneakyThrows;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerService<C extends Context> {

  private final ConcurrentLinkedQueue<TaskRun<C>> todoQueue = new ConcurrentLinkedQueue<>();
  private final List<ReactiveTask<C>> tasks;
  private final ExecutorService executorService;

  public WorkerService(List<ReactiveTask<C>> tasks, ExecutorService executorService) {
    this.tasks = tasks;
    this.executorService = executorService;
  }

  @SneakyThrows
  public void start() {
    //the producer
    createScheduler();

    //the worker
    Executors.newSingleThreadExecutor().execute(this::run);
  }

  private void createScheduler() {
    Executors.newSingleThreadExecutor().execute(() -> {
      for (int i = 0; i < 10; i++) {
        var input = (C) TestContext.builder()
          .testValue1(i)
          .id(i + "")
          .build();
        System.out.println("Scheduled another Task");
        todoQueue.add(new TaskRun<C>(CompletableFuture.completedFuture(input), new LinkedList<>(tasks.get(i % tasks.size()).getProcessorList())));

        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });
  }

  @SneakyThrows
  public void run() {

    System.out.println("Thread started");

    while (!todoQueue.isEmpty()) {
      var currentTask = todoQueue.remove();

      if (currentTask.getFuture().isDone()) {

        if (currentTask.getQueue().isEmpty()) {
          continue;
        }

        var nextProcessor = currentTask.getQueue().remove();
        C context = currentTask.getFuture().get();

        currentTask.setFuture(nextProcessor.processAsync(context, executorService));
      }

      todoQueue.add(currentTask);
    }

    System.out.println("Thread finished");
    executorService.shutdown();
  }
}
