package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.LogTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThrowingOutputTest extends LogTestBase {

  private static final String KEY = "Test";

  @Test
  void testBatchJobFlag() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .iterations(1)
        .build();

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .defaultOutputSource(new ThrowingOutputSource())
        .read(KEY, executor)
        .write("")
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(500, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    assertThatLogContainsMessage(
        "Error while preparing/processing processor 'DynamicOutputProcessor'. Shutting down gracefully");
  }

  static class ThrowingOutputSource implements MeshineryConnector<String, TestContext> {

    @Override
    public String getName() {
      return "default";
    }

    @Override
    public List<TestContext> getInputs(String key) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void writeOutput(String key, TestContext output) {
      throw new RuntimeException("Errror!");
    }
  }
}
