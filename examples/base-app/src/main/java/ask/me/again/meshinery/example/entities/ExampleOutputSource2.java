package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.example.TestContext2;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleOutputSource2 implements OutputSource<String, TestContext2> {

  @Override
  public void writeOutput(String key, TestContext2 output) {
    log.info("Received for topic: " + key);
  }
}
