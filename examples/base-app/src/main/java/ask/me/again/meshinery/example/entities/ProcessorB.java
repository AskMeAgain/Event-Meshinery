package ask.me.again.meshinery.example.entities;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.example.TestContext2;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ProcessorB implements MeshineryProcessor<TestContext2, TestContext2> {

  @Override
  public CompletableFuture<TestContext2> processAsync(TestContext2 context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {

      log.info("Rest call");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      log.info("Received: " + context.getTestValue1());

      return TestContext2.builder()
        .testValue1(context.getTestValue1() + 1)
        .build();

    }, executor);
  }
}
