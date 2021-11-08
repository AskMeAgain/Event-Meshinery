package ask.me.again.meshinery.core.common.processor;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.context.TestContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ErrorProcessor implements MeshineryProcessor<TestContext, TestContext> {

  @Override
  public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> {
      throw new RuntimeException();
    }, executor);
  }
}
