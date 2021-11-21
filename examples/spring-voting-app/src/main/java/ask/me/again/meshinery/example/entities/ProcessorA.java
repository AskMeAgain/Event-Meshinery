package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ProcessorA implements MeshineryProcessor<VotingContext, VotingContext> {

  @Override
  public CompletableFuture<VotingContext> processAsync(VotingContext context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Rest call");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      log.info("Received");

      return context;

    }, executor);
  }
}
