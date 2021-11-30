package ask.me.again.meshinery.example.entities;

import io.github.askmeagain.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.example.TestContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleOutputSource implements OutputSource<String, TestContext> {

  @Getter
  private final String name = "example-output";

  @Override
  public void writeOutput(String key, TestContext output) {
    log.info("Received for topic: " + key);
  }
}
