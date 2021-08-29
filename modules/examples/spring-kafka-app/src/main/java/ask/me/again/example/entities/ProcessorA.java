package ask.me.again.example.entities;

import ask.me.again.core.common.MeshineryProcessor;
import ask.me.again.example.TestContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class ProcessorA implements MeshineryProcessor<TestContext> {

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

      return context.toBuilder()
        .testValue1(context.getTestValue1() + 1)
        .build();

    }, executor);
  }
}
