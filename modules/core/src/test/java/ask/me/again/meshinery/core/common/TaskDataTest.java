package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.task.MeshineryTaskFactory;
import ask.me.again.meshinery.core.utils.AbstractTestBase;
import ask.me.again.meshinery.core.utils.context.TestContext;
import ask.me.again.meshinery.core.utils.processor.TaskDataTestProcessor;
import ask.me.again.meshinery.core.utils.sources.TestInputSource;
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

    OutputSource<String, TestContext> defaultOutput = Mockito.mock(OutputSource.class);

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(mockInputSource)
        .defaultOutputSource(defaultOutput)
        .read(INPUT_KEY, executor)
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
        .buildAndStart();

    var batchJobFinished = executor.awaitTermination(2, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(defaultOutput).writeOutput(any(), eq(expected));

  }
}
