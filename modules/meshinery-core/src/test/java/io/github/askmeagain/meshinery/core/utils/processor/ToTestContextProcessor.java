package io.github.askmeagain.meshinery.core.utils.processor;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.context.TestContext2;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class ToTestContextProcessor implements MeshineryProcessor<TestContext2, TestContext> {

  private final int index;

  @Override
  public CompletableFuture<TestContext> processAsync(TestContext2 context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> wait(context), executor);
  }

  @SneakyThrows
  private TestContext wait(TestContext2 context) {
    Thread.sleep(1000);
    return new TestContext(index + context.getIndex());
  }
}
