package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.example.TestContext;
import java.util.List;
import lombok.SneakyThrows;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleInputSource implements InputSource<String, TestContext> {

  private int counter = 0;

  @SneakyThrows
  @Override
  public List<TestContext> getInputs(String key) {

    Thread.sleep(500);
    counter++;

    return List.of(TestContext.builder()
        .id(counter + "")
        .testValue1(counter)
        .build());
  }
}
