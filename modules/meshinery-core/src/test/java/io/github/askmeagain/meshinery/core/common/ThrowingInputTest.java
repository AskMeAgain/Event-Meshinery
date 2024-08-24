package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.OutputCapture;
import io.github.askmeagain.meshinery.core.utils.sources.TestOutputSource;
import io.github.askmeagain.meshinery.core.utils.sources.ThrowingInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThrowingInputTest extends AbstractLogTestBase {

  private static final String KEY = "Test";

  @Test
  void testBatchJobFlag(OutputCapture output) throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var inputSource = new ThrowingInputSource();

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .outputSource(new TestOutputSource())
        .read(KEY)
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .executorService(executor)
        .gracePeriodMilliseconds(0)
        .build()
        .start();
    var batchJobFinished = executor.awaitTermination(1000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    assertThatLogContainsMessage(output, "Error while requesting new input data. Shutting down scheduler");
  }
}
