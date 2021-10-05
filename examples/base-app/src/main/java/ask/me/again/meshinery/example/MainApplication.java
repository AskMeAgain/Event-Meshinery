package ask.me.again.meshinery.example;

import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import ask.me.again.meshinery.example.entities.ExampleInputSource;
import ask.me.again.meshinery.example.entities.ExampleOutputSource;
import ask.me.again.meshinery.example.entities.ProcessorA;
import ask.me.again.meshinery.example.entities.ProcessorB;

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

    var tasks = List.of(
        new MeshineryTask<String, TestContext, TestContext>()
            .taskName("cool name")
            .outputSource(outputSource)
            .inputSource(inputSource)
            .read("topic-a", fixedThread)
            .process(processorA)
            .write("topic-b")
            .process(processorB)
            .write("topic-c")
            .process(processorA)
            .write("topic-d-FINISHED"),
        new MeshineryTask<String, TestContext, TestContext>()
            .taskName("Cool task 2")
            .outputSource(outputSource)
            .inputSource(inputSource)
            .read("topic-x", fixedThread)
            .process(processorA)
            .write("topic-y")
            .process(processorB)
            .write("topic-z")
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

}