package ask.me.again.example;

import ask.me.again.core.common.OutputSource;

import java.util.List;

public class ExampleOutputSource implements OutputSource<String, TestContext> {

  @Override
  public void writeOutput(String key, List<TestContext> output) {
    System.out.println("Received for topic: " + key);
  }
}
