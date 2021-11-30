package io.github.askmeagain.meshinery.core.utils.processor;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext2;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class TestContext2Processor implements MeshineryProcessor<TestContext2, TestContext2> {

  private final int index;

  @Override
  public CompletableFuture<TestContext2> processAsync(TestContext2 context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> wait(context), executor);
  }

  @SneakyThrows
  private TestContext2 wait(TestContext2 context) {
    Thread.sleep(1000);
    return new TestContext2(index + context.getIndex());
  }
}
