package ask.me.again.meshinery.core.utils.sources;

import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.utils.context.TestContext;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
public class TestInputSource implements InputSource<String, TestContext> {

  @Getter
  private final String name = "test-input";

  @Singular
  List<TestContext> todos;

  @Builder.Default
  int iterations = 1;
  int internalCounter;

  @Override
  public List<TestContext> getInputs(String key) {
    if (iterations == 0) {
      return Collections.emptyList();
    }

    iterations--;

    return todos.stream()
                .map(testContext -> testContext.withId((++internalCounter) + ""))
                .toList();
  }
}