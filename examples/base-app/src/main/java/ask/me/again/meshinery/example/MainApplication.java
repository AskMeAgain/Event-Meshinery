package ask.me.again.meshinery.example;

import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import ask.me.again.meshinery.example.entities.ExampleInputSource;
import ask.me.again.meshinery.example.entities.ExampleOutputSource;
import ask.me.again.meshinery.example.entities.ProcessorA;

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

    var atomicBoolean = new AtomicBoolean(false);

    var inputSource = new ExampleInputSource();
    var outputSource = new ExampleOutputSource();

    var tasks = List.of(
      MeshineryTask.<String, TestContext>builder()
        .taskName("cool name")
        .outputSource(outputSource)
        .inputSource(inputSource)
        .read("topic-a", fixedThread)
        .process(processor)
        .write("topic-b")
        .process(processor)
        .write("topic-c")
        .process(processor)
        .write("topic-d-FINISHED")
        .build(),
      MeshineryTask.<String, TestContext>builder()
        .taskName("Cool task 2")
        .outputSource(outputSource)
        .inputSource(inputSource)
        .read("topic-x", fixedThread)
        .process(processor)
        .write("topic-y")
        .process(processor)
        .write("topic-z")
        .process(processor)
        .write("topic-w-FINISHED")
        .build());

    new RoundRobinScheduler<>(tasks, true).start(atomicBoolean);

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