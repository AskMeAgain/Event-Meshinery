package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.ListProcessor;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.common.context.TestContext;
import ask.me.again.meshinery.core.common.processor.TestContextProcessor;
import ask.me.again.meshinery.core.common.processor.ToTestContext2Processor;
import ask.me.again.meshinery.core.common.processor.ToTestContextProcessor;
import ask.me.again.meshinery.core.common.sources.TestInputSource;
import ask.me.again.meshinery.core.processors.ParallelProcessor;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

public class ComplexParallelizationTest {

  @Test
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
            .parallel(ListProcessor.<TestContext>builder()
                .process(new ToTestContext2Processor(1))
                .process(new ToTestContextProcessor(2)))
            .parallel(new TestContextProcessor(30))
            .parallel(new TestContextProcessor(30))
            .parallel(new TestContextProcessor(30))
            .combine(ComplexParallelizationTest::getCombine))
        .write("");

    //Act -------------------------------------------------------------------------------------
    new RoundRobinScheduler<>(true, List.of(task)).start();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    //Assert ----------------------------------------------------------------------------------
    var argumentCapture = ArgumentCaptor.forClass(TestContext.class);
    Mockito.verify(outputMock).writeOutput(eq(""), argumentCapture.capture());
    assertThat(argumentCapture.getValue())
        .extracting(TestContext::getIndex)
        .isEqualTo(93);
  }

  private static TestContext getCombine(List<TestContext> list) {
    var sum = list.stream().mapToInt(TestContext::getIndex).sum();
    return new TestContext(sum);
  }
}
