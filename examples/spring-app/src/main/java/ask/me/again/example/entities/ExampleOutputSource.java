package ask.me.again.example.entities;

import ask.me.again.core.common.OutputSource;
import ask.me.again.example.TestContext;
import org.springframework.stereotype.Component;

@Component
public class ExampleOutputSource implements OutputSource<String, TestContext> {

  @Override
  public void writeOutput(String key, TestContext output) {
    System.out.println("Received for topic: " + key);
  }
}
