package io.github.askmeagain.meshinery.core.e2e.base;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.SLEEP_IN_PROCESSOR;

@Slf4j
public class E2eTestProcessor implements MeshineryProcessor<TestContext, TestContext> {
  @Override
  public TestContext processAsync(TestContext context) {
    wait1Sec();
    return context;
  }

  @SneakyThrows
  private void wait1Sec() {
    Thread.sleep(SLEEP_IN_PROCESSOR);
  }
}
