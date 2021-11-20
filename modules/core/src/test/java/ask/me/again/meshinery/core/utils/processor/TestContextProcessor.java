package ask.me.again.meshinery.core.utils.processor;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.utils.context.TestContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class TestContextProcessor implements MeshineryProcessor<TestContext, TestContext> {

  private final int index;

  @Override
  public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
    int i = 0;
    return CompletableFuture.supplyAsync(() -> wait(context), executor);
  }

  @SneakyThrows
  private TestContext wait(TestContext context) {
    Thread.sleep(1000);
    return new TestContext(index + context.getIndex());
  }
}
