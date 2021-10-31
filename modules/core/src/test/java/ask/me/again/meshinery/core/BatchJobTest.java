package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.RoundRobinScheduler;
import ask.me.again.meshinery.core.common.context.TestContext;
import ask.me.again.meshinery.core.common.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

class BatchJobTest {

  private static final String KEY = "Test";
  private static final int ITERATIONS = 4;

  @RepeatedTest(10)
  void testBatchJobFlag() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .iterations(ITERATIONS)
        .build();
    var mockInputSource = Mockito.spy(inputSource);
    var executor = Executors.newSingleThreadExecutor();

    var task = MeshineryTask.<String, TestContext>builder()
        .inputSource(mockInputSource)
        .read(KEY, executor);

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .build();
    var batchJobFinished = executor.awaitTermination(500, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(mockInputSource, Mockito.times(ITERATIONS + 1)).getInputs(eq(KEY));
  }
}
