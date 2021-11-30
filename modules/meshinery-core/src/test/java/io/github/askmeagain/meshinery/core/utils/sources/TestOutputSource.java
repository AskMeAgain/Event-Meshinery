package io.github.askmeagain.meshinery.core.utils.sources;

import io.github.askmeagain.meshinery.core.common.OutputSource;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import lombok.Getter;

public class TestOutputSource implements OutputSource<String, TestContext> {
  @Getter
  private final String name = "null";

  @Override
  public void writeOutput(String key, TestContext output) {
  }
}
