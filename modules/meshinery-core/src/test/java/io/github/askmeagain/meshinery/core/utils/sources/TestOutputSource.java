package io.github.askmeagain.meshinery.core.utils.sources;

import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import lombok.Getter;

public class TestOutputSource implements MeshinerySourceConnector<String, TestContext> {
  @Getter
  private final String name = "null";

  @Override
  public void writeOutput(String key, TestContext output, TaskData taskData) {
  }

  @Override
  public List<TestContext> getInputs(List<String> key) {
    throw new UnsupportedOperationException();
  }
}
