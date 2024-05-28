package io.github.askmeagain.meshinery.core.utils.processor;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;

public class TaskDataTestProcessor implements MeshineryProcessor<TestContext, TestContext> {

  @Override
  public TestContext processAsync(TestContext context) {
    return context.toBuilder()
        .id(context.getIndex() + getTaskData().getSingle("test"))
        .index(Integer.parseInt(context.getIndex() + getTaskData().getSingle("test")))
        .build();
  }
}
