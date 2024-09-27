package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.processors.ParallelProcessor;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.utils.AbstractTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class SimpleParallelizationTest extends AbstractTestBase {

  public static final String KEY = "Test";

  @RepeatedTest(10)
  @SuppressWarnings("unchecked")
  void testSimpleParallelization() throws InterruptedException {
    //Arrange ---------------------------------------------------------------------------------
    var executor = Executors.newFixedThreadPool(3);

    MeshinerySourceConnector<String, TestContext> outputSource = Mockito.mock(MeshinerySourceConnector.class);

    var inputSource = TestInputSource.builder()
        .todo(new TestContext(0))
        .build();

    var task = MeshineryTask.<String, TestContext>builder()
        .read(KEY)
        .inputSource(inputSource)
        .outputSource(outputSource)
        .process(ParallelProcessor.<TestContext>builder()
            .executor(executor)
            .parallel(new TestContextProcessor(3))
            .parallel(new TestContextProcessor(3))
            .combine(this::getCombine))
        .write(KEY)
        .build();

    //Act -------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .batchJob(true)
        .task(task)
        .executorService(executor)
        .gracePeriodMilliseconds(1000)
        .build()
        .start();
    var batchJobFinished = executor.awaitTermination(1500, TimeUnit.MILLISECONDS);

    //Assert ----------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(outputSource).writeOutput(eq(KEY), any(), any());
  }
}
