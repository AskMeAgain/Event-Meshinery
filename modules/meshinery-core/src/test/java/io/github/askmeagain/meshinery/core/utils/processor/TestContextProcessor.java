package io.github.askmeagain.meshinery.core.utils.processor;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TestContextProcessor implements MeshineryProcessor<TestContext, TestContext> {

  private final int index;

  @Override
  public TestContext processAsync(TestContext context) {
    log.info("Computing stuff");
    return wait(context);
  }

  @SneakyThrows
  private TestContext wait(TestContext context) {
    Thread.sleep(1000);
    return new TestContext(index + context.getIndex());
  }
}
