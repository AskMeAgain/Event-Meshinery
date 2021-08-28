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
      System.out.println("Received: " + context.getTestvalue1());

      return context.toBuilder()
        .testvalue1(context.getTestvalue1() + 1)
        .build();

    });
  }
}
