package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import io.github.askmeagain.meshinery.core.utils.sources.ThrowingOutputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThrowingOutputTest extends AbstractLogTestBase {

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
        .outputSource(new ThrowingOutputSource())
        .read(executor, KEY)
        .write("")
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .gracePeriodMilliseconds(0)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(500, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    assertThatLogContainsMessage(
        "Error while preparing/processing processor 'DynamicOutputProcessor'. Shutting down gracefully");
  }

  @Override
  protected Class<?> loggerToUse() {
    return RoundRobinScheduler.class;
  }
}
