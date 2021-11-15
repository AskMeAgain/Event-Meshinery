package ask.me.again.meshinery.core.utils.sources;

import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.utils.context.TestContext;
import lombok.Getter;

public class TestOutputSource implements OutputSource<String, TestContext> {
  @Getter
  private final String name = "null";

  @Override
  public void writeOutput(String key, TestContext output) {
  }
}
