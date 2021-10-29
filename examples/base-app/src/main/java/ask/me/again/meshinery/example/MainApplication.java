package ask.me.again.meshinery.example;

import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import ask.me.again.meshinery.example.entities.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class MainApplication {

  public static void main(String[] input) {

    var processorA = new ProcessorA();
    var processorB = new ProcessorB();
    var fixedThread = Executors.newFixedThreadPool(4);

    var inputSource = new ExampleInputSource();
    var outputSource = new ExampleOutputSource();
    var outputSource2 = new ExampleOutputSource2();

    var tasks = List.of(
        MeshineryTask.<String, TestContext>builder()
            .taskName("cool name")
            .defaultOutputSource(outputSource)
            .inputSource(inputSource)
            .read("topic-a", fixedThread)
            .process(processorA)
            .write("topic-b")
            .contextSwitch(outputSource2, MainApplication::create)
            .process(processorB)
            .write("topic-c")
            .contextSwitch(outputSource, MainApplication::create2)
            .process(processorA)
            .write("topic-d-FINISHED"),
        MeshineryTask.<String, TestContext>builder()
            .taskName("Cool task 2")
            .defaultOutputSource(outputSource)
            .inputSource(inputSource)
            .read("topic-x", fixedThread)
            .process(processorA)
            .write("topic-y")
            .contextSwitch(outputSource2, MainApplication::create)
            .process(processorB)
            .write("topic-z")
            .contextSwitch(outputSource, MainApplication::create2)
            .process(processorA)
            .write("topic-w-FINISHED")
    );

    var scheduler = new RoundRobinScheduler<>(true, tasks);
    scheduler.start();

    CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("turning off");
      scheduler.gracefulShutdown();
    });

  }

  private static TestContext create2(TestContext2 context) {
    return TestContext.builder()
        .id(context.getId())
        .testValue1(context.getTestValue1())
        .build();
  }

  private static TestContext2 create(TestContext context) {
    return TestContext2.builder()
        .id(context.getId())
        .testValue1(context.getTestValue1())
        .build();
  }
}