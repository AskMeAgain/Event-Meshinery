package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.AbstractTestBase;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.common.RoundRobinScheduler;
import ask.me.again.meshinery.core.common.context.TestContext;
import ask.me.again.meshinery.core.common.processor.TestContextProcessor;
import ask.me.again.meshinery.core.common.sources.TestInputSource;
import ask.me.again.meshinery.core.processors.ParallelProcessor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class SimpleParallelizationTest extends AbstractTestBase {

  public static final String KEY = "Test";

  @Test
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
    executor.awaitTermination(4, TimeUnit.SECONDS);

    //Assert ----------------------------------------------------------------------------------
    Mockito.verify(outputSource).writeOutput(eq(KEY), any());

  }
}
