package ask.me.again.meshinery.core.common.processor;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.context.TestContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class TestContextProcessor implements MeshineryProcessor<TestContext, TestContext> {

  private final int index;

  @Override
  public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> wait(context), executor);
  }

  @SneakyThrows
  private TestContext wait(TestContext context) {
    Thread.sleep(3000);
    return new TestContext(index + context.getIndex());
  }
}
