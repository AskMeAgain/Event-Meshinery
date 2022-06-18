package io.github.askmeagain.meshinery.core.utils.processor;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TestContextLongerWaitProcessor implements MeshineryProcessor<TestContext, TestContext> {

  private final int index;

  @Override
  public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      log.info("Computing stuff");
      return wait(context);
    }, executor);
  }

  @SneakyThrows
  private TestContext wait(TestContext context) {
    Thread.sleep(100000);
    return new TestContext(index + context.getIndex());
  }
}
