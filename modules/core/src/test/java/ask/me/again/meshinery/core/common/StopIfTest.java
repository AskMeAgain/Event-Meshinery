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
import static org.mockito.ArgumentMatchers.eq;

class StopIfTest {

  public static final String KEY = "Test";
  public static final TestContext EXPECTED = new TestContext("2", 1);

  @Test
  @SuppressWarnings("unchecked")
  void testStopIf() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    OutputSource<String, TestContext> outputSource = Mockito.mock(OutputSource.class);

    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .todo(new TestContext(1))
        .build();

    var executor = Executors.newSingleThreadExecutor();
    var task = MeshineryTask.<String, TestContext>builder()
        .inputSource(inputSource)
        .defaultOutputSource(outputSource)
        .read(KEY, executor)
        .stopIf(x -> x.getIndex() == 0)
        .write(KEY);

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(2, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(outputSource).writeOutput(eq(KEY), eq(EXPECTED));
  }
}
