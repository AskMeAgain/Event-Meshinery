package ask.me.again.example;

import ask.me.again.core.common.ReactiveTask;
import ask.me.again.core.service.WorkerService;
import ask.me.again.example.entities.ExampleInputSource;
import ask.me.again.example.entities.ExampleOutputSource;
import ask.me.again.example.entities.ProcessorA;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainApplication {

  public static void main(String[] input) {

    var processor = new ProcessorA();
    var singleThread = Executors.newSingleThreadExecutor();
    var fixedThread = new ThreadPoolExecutor(4, 30, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());

    var atomicBoolean = new AtomicBoolean(true);

    var inputSource = new ExampleInputSource();
    var outputSource = new ExampleOutputSource();

    var tasks = List.of(
      ReactiveTask.<String, TestContext>builder()
        .taskName("cool name")
        .outputSource(outputSource)
        .read("topic-a", fixedThread)
        .process(processor)
        .write("topic-b")
        .process(processor)
        .write("topic-c")
        .process(processor)
        .write("topic-d-FINISHED")
        .build(),
      ReactiveTask.<String, TestContext>builder()
        .outputSource(outputSource)
        .taskName("Cool task 2")
        .read("topic-x", fixedThread)
        .process(processor)
        .write("topic-y")
        .process(processor)
        .write("topic-z")
        .process(processor)
        .write("topic-w-FINISHED")
        .build());

    new WorkerService<>(tasks, inputSource).start(atomicBoolean);

    CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(15000);
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