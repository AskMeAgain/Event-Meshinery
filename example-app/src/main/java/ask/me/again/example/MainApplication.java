package ask.me.again.example;

import ask.me.again.core.builder.ReactiveTask;
import ask.me.again.core.worker.WorkerService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainApplication {

  public static void main(String[] input) {

    var processor = new ProcessorA();
    var singleThread = Executors.newSingleThreadExecutor();
    var fixedThread = Executors.newFixedThreadPool(4);

    var atomicBoolean = new AtomicBoolean(true);

    var inputSource = new ExampleInputSource();
    var outputSource = new ExampleOutputSource();

    var tasks = List.of(
      ReactiveTask.<String, TestContext>builder()
        .taskName("cool name")
        .outputSource(outputSource)
        .read("topic-a", singleThread)
        .process(processor)
        .write("topic-b")
        .process(processor)
        .write("topic-b")
        .process(processor)
        .write("topic-b-FINISHED")
        .build(),
      ReactiveTask.<String, TestContext>builder()
        .outputSource(outputSource)
        .taskName("Cool task 2")
        .read("topic-a", fixedThread)
        .process(processor)
        .write("topic-b")
        .process(processor)
        .write("topic-b")
        .process(processor)
        .write("topic-c-FINISHED")
        .build());

    new WorkerService<>(tasks, inputSource).start(atomicBoolean);

    CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      shutdownWorkers(singleThread, fixedThread, atomicBoolean);
    });

  }

  private static void shutdownWorkers(ExecutorService singleThread, ExecutorService fixedThread, AtomicBoolean atomicBoolean) {
    System.out.println("turning off");
    atomicBoolean.set(false);
    fixedThread.shutdown();
    singleThread.shutdown();
  }
}