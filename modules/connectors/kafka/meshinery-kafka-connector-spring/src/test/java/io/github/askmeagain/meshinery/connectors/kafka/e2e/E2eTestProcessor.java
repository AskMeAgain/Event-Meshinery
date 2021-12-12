package io.github.askmeagain.meshinery.connectors.kafka.e2e;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.SneakyThrows;

import static io.github.askmeagain.meshinery.connectors.kafka.e2e.E2eTestConfiguration.SLEEP_IN_PROCESSOR;

public class E2eTestProcessor implements MeshineryProcessor<TestContext, TestContext> {
  @Override
  public CompletableFuture<TestContext> processAsync(
      TestContext context, Executor executor
  ) {
    return CompletableFuture.supplyAsync(() -> {
      wait1Sec();
      return context;
    }, executor);
  }

  @SneakyThrows
  private void wait1Sec() {
    Thread.sleep(SLEEP_IN_PROCESSOR);
  }
}
