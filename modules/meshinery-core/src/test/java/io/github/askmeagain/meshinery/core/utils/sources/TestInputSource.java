package io.github.askmeagain.meshinery.core.utils.sources;

import io.github.askmeagain.meshinery.core.common.InputSource;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@AllArgsConstructor
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
      log.info("Stopping TestInputSource");
      return Collections.emptyList();
    }

    iterations--;
    var maxValue = 1 + (internalCounter / todos.size() + iterations);
    log.info("Iteration '{}' out of '{}'", maxValue - iterations, maxValue);
    return todos.stream()
        .map(testContext -> testContext.withId((++internalCounter) + ""))
        .toList();
  }
}
