package io.github.askmeagain.meshinery.core.utils.processor;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.context.TestContext2;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class ToTestContext2Processor implements MeshineryProcessor<TestContext, TestContext2> {

  private final int index;

  @Override
  public CompletableFuture<TestContext2> processAsync(TestContext context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> wait(context), executor);
  }

  @SneakyThrows
  private TestContext2 wait(TestContext context) {
    Thread.sleep(1000);
    return new TestContext2(index + context.getIndex());
  }
}
