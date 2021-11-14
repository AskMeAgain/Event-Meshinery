package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.core.utils.context.TestContext;
import ask.me.again.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class WriteTest {

  public static final String KEY = "Test";
  public static final int ITERATIONS = 2;

  @Test
  @SuppressWarnings("unchecked")
  void writeTest() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();

    var mockInputSource = Mockito.spy(inputSource);
    OutputSource<String, TestContext> mockOutputSource = Mockito.mock(OutputSource.class);
    OutputSource<String, TestContext> defaultOutputSource = Mockito.mock(OutputSource.class);
    var executor = Executors.newSingleThreadExecutor();

    var task = MeshineryTask.<String, TestContext>builder()
        .inputSource(mockInputSource)
        .defaultOutputSource(defaultOutputSource)
        .read(KEY, executor)
        .write(KEY, mockOutputSource)
        .write(KEY, KEY);

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(1, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();

    Mockito.verify(mockInputSource, Mockito.times(ITERATIONS)).getInputs(eq(KEY));
    Mockito.verify(mockOutputSource).writeOutput(any(), any());
    Mockito.verify(defaultOutputSource, Mockito.times(2)).writeOutput(any(), any());
  }
}
