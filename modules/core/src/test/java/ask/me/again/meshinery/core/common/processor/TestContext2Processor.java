package ask.me.again.meshinery.core.common.processor;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.context.TestContext2;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class TestContext2Processor implements MeshineryProcessor<TestContext2, TestContext2> {

  private final int index;

  @Override
  public CompletableFuture<TestContext2> processAsync(TestContext2 context, Executor executor) {
    return CompletableFuture.supplyAsync(() -> wait(context), executor);
  }

  @SneakyThrows
  private TestContext2 wait(TestContext2 context) {
    Thread.sleep(3000);
    return new TestContext2(index + context.getIndex());
  }
}
