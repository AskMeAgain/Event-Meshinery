package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.example.TestContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class ProcessorA implements MeshineryProcessor<TestContext, TestContext> {

  @Override
  public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {

      System.out.println("Rest call");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("Received: " + context.getTestValue1());

      return TestContext.builder()
        .testValue1(context.getTestValue1() + 1)
        .build();

    }, executor);
  }
}
