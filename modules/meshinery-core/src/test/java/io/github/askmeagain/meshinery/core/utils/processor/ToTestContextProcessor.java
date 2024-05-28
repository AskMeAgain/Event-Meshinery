package io.github.askmeagain.meshinery.core.utils.processor;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.context.TestContext2;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class ToTestContextProcessor implements MeshineryProcessor<TestContext2, TestContext> {

  private final int index;

  @Override
  public TestContext processAsync(TestContext2 context) {
    return wait(context);
  }

  @SneakyThrows
  private TestContext wait(TestContext2 context) {
    Thread.sleep(1000);
    return new TestContext(index + context.getIndex());
  }
}
