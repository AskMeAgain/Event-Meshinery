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
    log.info("INSIDE THE PROCESSOR!!!");
    return CompletableFuture.supplyAsync(() -> {
      log.info("INSIDE THE NEW THREAD");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      log.info("RECEIVED NEW THREAD");

      return context;

    }, executor);
  }
}
