package io.github.askmeagain.meshinery.core.e2e.base;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.SLEEP_IN_PROCESSOR;

@Slf4j
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
