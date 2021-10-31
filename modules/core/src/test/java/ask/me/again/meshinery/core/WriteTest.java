package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.common.RoundRobinScheduler;
import ask.me.again.meshinery.core.common.context.TestContext;
import ask.me.again.meshinery.core.common.sources.TestInputSource;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mockito;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class WriteTest {

  public static final String KEY = "Test";
  public static final int ITERATIONS = 2;

  @RepeatedTest(10)
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
        .build();
    var batchJobFinished = executor.awaitTermination(1, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();

    Mockito.verify(mockInputSource, Mockito.times(ITERATIONS)).getInputs(eq(KEY));
    Mockito.verify(mockOutputSource).writeOutput(any(), any());
    Mockito.verify(defaultOutputSource, Mockito.times(2)).writeOutput(any(), any());
  }
}
