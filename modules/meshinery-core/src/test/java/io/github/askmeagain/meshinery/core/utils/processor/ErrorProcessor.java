package io.github.askmeagain.meshinery.core.utils.processor;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;

public class ErrorProcessor implements MeshineryProcessor<TestContext, TestContext> {

  @Override
  public TestContext process(TestContext context) {
      throw new RuntimeException();
  }
}
