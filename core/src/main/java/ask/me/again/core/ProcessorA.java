package ask.me.again.core;

import java.util.concurrent.CompletableFuture;

public class ProcessorA implements ReactiveProcessor<TestContext> {

  @Override
  public CompletableFuture<TestContext> processAsync(TestContext context) {
    return CompletableFuture.supplyAsync(() -> {

      System.out.println("Rest call");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("Received: " + context.getTestValue1());

      return context.toBuilder()
        .testValue1(context.getTestValue1() + 1)
        .build();

    });
  }
}
