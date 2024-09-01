package io.github.askmeagain.meshinery.core.utils.processor;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext2;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class TestContext2Processor implements MeshineryProcessor<TestContext2, TestContext2> {

  private final int index;

  @Override
  public TestContext2 process(TestContext2 context) {
    return wait(context);
  }

  @SneakyThrows
  private TestContext2 wait(TestContext2 context) {
    Thread.sleep(1000);
    return new TestContext2(index + context.getIndex());
  }
}
