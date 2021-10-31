package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.AbstractTestBase;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.common.RoundRobinScheduler;
import ask.me.again.meshinery.core.common.context.TestContext;
import ask.me.again.meshinery.core.common.processor.TestContextProcessor;
import ask.me.again.meshinery.core.common.sources.TestInputSource;
import ask.me.again.meshinery.core.processors.ParallelProcessor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class SimpleParallelizationTest extends AbstractTestBase {

  public static final String KEY = "Test";

  @RepeatedTest(10)
  @SuppressWarnings("unchecked")
  void testSimpleParallelization() throws InterruptedException {
    //Arrange ---------------------------------------------------------------------------------
    var executor = Executors.newFixedThreadPool(3);

    OutputSource<String, TestContext> outputSource = Mockito.mock(OutputSource.class);

    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();

    var task = MeshineryTask.<String, TestContext>builder()
        .read(KEY, executor)
        .inputSource(inputSource)
        .defaultOutputSource(outputSource)
        .process(ParallelProcessor.<TestContext>builder()
            .parallel(new TestContextProcessor(3))
            .parallel(new TestContextProcessor(3))
            .combine(this::getCombine))
        .write(KEY);

    //Act -------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .build();
    var batchJobFinished = executor.awaitTermination(1500, TimeUnit.MILLISECONDS);

    //Assert ----------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(outputSource).writeOutput(eq(KEY), any());

  }
}
