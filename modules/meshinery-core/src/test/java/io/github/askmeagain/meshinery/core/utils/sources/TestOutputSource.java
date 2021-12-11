package io.github.askmeagain.meshinery.core.utils.sources;

import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import lombok.Getter;

public class TestOutputSource implements MeshineryConnector<String, TestContext> {
  @Getter
  private final String name = "null";

  @Override
  public void writeOutput(String key, TestContext output) {
  }

  @Override
  public List<TestContext> getInputs(String key) {
    throw new UnsupportedOperationException();
  }
}
