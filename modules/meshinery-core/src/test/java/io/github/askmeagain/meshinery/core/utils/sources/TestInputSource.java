package io.github.askmeagain.meshinery.core.utils.sources;

import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@AllArgsConstructor
public class TestInputSource implements MeshinerySourceConnector<String, TestContext> {

  @Getter
  private final String name = "test-input";
  @Singular
  List<TestContext> todos;
  @Builder.Default
  int iterations = 1;
  int internalCounter;
  @Builder.Default
  int delayMilliseconds = 0;

  @Override
  public List<TestContext> getInputs(List<String> keys) {
    return getInputs("");
  }

  @SneakyThrows
  private synchronized List<TestContext> getInputs(String key) {
    if (iterations == 0) {
      iterations--;
      log.info("Stopping TestInputSource");
      return Collections.emptyList();
    }

    if (iterations == -1) {
      return Collections.emptyList();
    }

    if (delayMilliseconds > 0) {
      Thread.sleep(delayMilliseconds);
    }

    iterations--;
    var maxValue = 1 + (internalCounter / todos.size() + iterations);
    //log.info("Iteration '{}' out of '{}'", maxValue - iterations, maxValue);
    return todos.stream()
        .map(testContext -> testContext.withId((++internalCounter) + ""))
        //.peek(x -> log.info("Input: " + x.getId()))
        .toList();
  }

  @Override
  public void writeOutput(String key, TestContext output, TaskData taskData) {
    throw new UnsupportedOperationException();
  }
}
