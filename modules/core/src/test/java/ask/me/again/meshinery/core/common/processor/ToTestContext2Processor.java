package ask.me.again.meshinery.core.common.processor;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.context.TestContext;
import ask.me.again.meshinery.core.common.context.TestContext2;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
