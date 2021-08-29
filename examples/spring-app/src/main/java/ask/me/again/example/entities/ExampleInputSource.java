package ask.me.again.example.entities;

import ask.me.again.core.common.InputSource;
import ask.me.again.example.TestContext;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
