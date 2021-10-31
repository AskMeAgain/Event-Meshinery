package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.example.TestContext2;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleOutputSource2 implements OutputSource<String, TestContext2> {

  @Override
  public void writeOutput(String key, TestContext2 output) {
    System.out.println("Received for topic: " + key);
  }
}
