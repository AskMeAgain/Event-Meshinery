package ask.me.again.example;

import ask.me.again.core.common.InputSource;
import lombok.SneakyThrows;

import java.util.List;

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
