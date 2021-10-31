package ask.me.again.meshinery.core.common.sources;

import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.context.TestContext;
import lombok.Builder;
import lombok.Singular;

import java.util.Collections;
import java.util.List;

@Builder
public class TestInputSource implements InputSource<String, TestContext> {

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
