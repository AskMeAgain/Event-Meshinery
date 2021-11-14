package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.processors.FluidProcessor;
import ask.me.again.meshinery.core.processors.ParallelProcessor;
import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.core.utils.AbstractTestBase;
import ask.me.again.meshinery.core.utils.context.TestContext;
import ask.me.again.meshinery.core.utils.processor.TestContextProcessor;
import ask.me.again.meshinery.core.utils.processor.ToTestContext2Processor;
import ask.me.again.meshinery.core.utils.processor.ToTestContextProcessor;
import ask.me.again.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

class ComplexParallelizationTest extends AbstractTestBase {

  @RepeatedTest(10)
  @SuppressWarnings("unchecked")
  void testComplexParallelization() throws InterruptedException {
    //Arrange ---------------------------------------------------------------------------------
    var executor = Executors.newFixedThreadPool(3);
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();

    OutputSource<String, TestContext> outputMock = Mockito.mock(OutputSource.class);

    var task = MeshineryTask.<String, TestContext>builder()
        .read("Test", executor)
        .inputSource(inputSource)
        .defaultOutputSource(outputMock)
        .process(ParallelProcessor.<TestContext>builder()
            .parallel(FluidProcessor.<TestContext>builder()
                .process(new ToTestContext2Processor(1))
                .process(new ToTestContextProcessor(2)))
            .parallel(new TestContextProcessor(30))
            .parallel(new TestContextProcessor(30))
            .parallel(new TestContextProcessor(30))
            .combine(this::getCombine))
        .write("");

    //Act -------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(3, TimeUnit.SECONDS);

    //Assert ----------------------------------------------------------------------------------
    var argumentCapture = ArgumentCaptor.forClass(TestContext.class);
    Mockito.verify(outputMock).writeOutput(eq(""), argumentCapture.capture());
    assertThat(batchJobFinished).isTrue();
    assertThat(argumentCapture.getValue())
        .extracting(TestContext::getIndex)
        .isEqualTo(93);
  }
}
