package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.example.TestContext2;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ProcessorB implements MeshineryProcessor<TestContext2, TestContext2> {

  @Override
  public CompletableFuture<TestContext2> processAsync(TestContext2 context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {

      System.out.println("Rest call");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("Received: " + context.getTestValue1());

      return TestContext2.builder()
        .testValue1(context.getTestValue1() + 1)
        .build();

    }, executor);
  }
}
