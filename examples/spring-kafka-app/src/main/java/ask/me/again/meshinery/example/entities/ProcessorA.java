package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.example.TestContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ProcessorA implements MeshineryProcessor<TestContext, TestContext> {

  @Override
  public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {

      log.info("Rest call");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      log.info("Received: " + context.getTestValue1());

      return context.toBuilder()
                    .testValue1(context.getTestValue1() + 1)
                    .build();

    }, executor);
  }
}
