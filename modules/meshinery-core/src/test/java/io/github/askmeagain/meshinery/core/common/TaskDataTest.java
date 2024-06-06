package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.AbstractTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TaskDataTestProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class TaskDataTest extends AbstractTestBase {

  private static final String INPUT_KEY = "Test";

  @Test
  @SuppressWarnings("unchecked")
  void taskDataTest() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();
    var mockInputSource = Mockito.spy(inputSource);

    var executor = Executors.newSingleThreadExecutor();

    MeshinerySourceConnector<String, TestContext> defaultOutput = Mockito.mock(MeshinerySourceConnector.class);

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(mockInputSource)
        .outputSource(defaultOutput)
        .read(INPUT_KEY)
        .process(new TaskDataTestProcessor())
        .write(INPUT_KEY)
        .putData("test", "1234")
        .build();

    var expected = new TestContext(0).toBuilder()
        .id("01234")
        .index(1234)
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .executorService(executor)
        .gracePeriodMilliseconds(0)
        .build()
        .start();

    var batchJobFinished = executor.awaitTermination(2, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(defaultOutput).writeOutput(any(), eq(expected), any());
  }
}
