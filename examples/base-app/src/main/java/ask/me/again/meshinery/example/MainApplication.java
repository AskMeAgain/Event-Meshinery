package ask.me.again.meshinery.example;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import ask.me.again.meshinery.example.entities.ExampleInputSource;
import ask.me.again.meshinery.example.entities.ExampleOutputSource;
import ask.me.again.meshinery.example.entities.ExampleOutputSource2;
import ask.me.again.meshinery.example.entities.ProcessorA;
import ask.me.again.meshinery.example.entities.ProcessorB;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MainApplication {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static void main(String[] input) {

    var processorA = new ProcessorA();
    var processorB = new ProcessorB();
    var fixedThread = Executors.newFixedThreadPool(4);

    var inputSource = new ExampleInputSource();
    var outputSource = new ExampleOutputSource();
    var outputSource2 = new ExampleOutputSource2();

    var task1 = MeshineryTaskFactory.<String, TestContext>builder()
        .taskName("cool name")
        .defaultOutputSource(outputSource)
        .inputSource(inputSource)
        .read("topic-a", fixedThread)
        .process(processorA)
        .write("topic-b")
        .contextSwitch(outputSource2, MainApplication::create, Collections.emptyList())
        .process(processorB)
        .write("topic-c")
        .contextSwitch(outputSource, MainApplication::create2, Collections.emptyList())
        .process(processorA)
        .write("topic-d-FINISHED")
        .build();

    var task2 = MeshineryTaskFactory.<String, TestContext>builder()
        .taskName("Cool task 2")
        .defaultOutputSource(outputSource)
        .inputSource(inputSource)
        .read("topic-x", fixedThread)
        .process(processorA)
        .write("topic-y")
        .contextSwitch(outputSource2, MainApplication::create, Collections.emptyList())
        .process(processorB)
        .write("topic-z")
        .contextSwitch(outputSource, MainApplication::create2, Collections.emptyList())
        .process(processorA)
        .write("topic-w-FINISHED")
        .build();

    var scheduler = RoundRobinScheduler.builder()
        .isBatchJob(true)
        .tasks(List.of(task1, task2))
        .buildAndStart();

    CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      log.info("turning off");
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