package io.github.askmeagain.meshinery.core.utils.sources;

import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;

public class ThrowingInputSource implements MeshineryConnector<String, TestContext> {

  @Override
  public String getName() {
    return "default";
  }

  @Override
  public void writeOutput(String key, TestContext output) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<TestContext> getInputs(List<String> key) {
    throw new RuntimeException("Error!");
  }
}
