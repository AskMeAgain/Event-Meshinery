package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.LogTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestOutputSource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThrowingInputTest extends LogTestBase {

  private static final String KEY = "Test";

  @Test
  void testBatchJobFlag() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var inputSource = new ThrowingInputSource();

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .defaultOutputSource(new TestOutputSource())
        .read(KEY, executor)
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(500, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    assertThatLogContainsMessage("Error while requesting new input data. Shutting down scheduler");
  }

  static class ThrowingInputSource implements InputSource<String, TestContext> {

    @Override
    public String getName() {
      return "default";
    }

    @Override
    public List<TestContext> getInputs(String key) {
      throw new RuntimeException("Errror!");
    }
  }
}
