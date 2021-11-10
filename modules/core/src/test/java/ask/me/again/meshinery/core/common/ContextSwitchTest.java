package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.utils.AbstractTestBase;
import ask.me.again.meshinery.core.utils.context.TestContext;
import ask.me.again.meshinery.core.utils.context.TestContext2;
import ask.me.again.meshinery.core.utils.processor.TestContext2Processor;
import ask.me.again.meshinery.core.utils.processor.TestContextProcessor;
import ask.me.again.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

class ContextSwitchTest extends AbstractTestBase {

  private static final String INPUT_KEY = "Test";

  private static final TestContext EXPECTED = new TestContext(2);

  @Test
  @SuppressWarnings("unchecked")
  void contextSwitchTest() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();
    var mockInputSource = Mockito.spy(inputSource);

    var processorA = Mockito.spy(new TestContextProcessor(0));
    var processorB = Mockito.spy(new TestContext2Processor(0));

    var executor = Executors.newSingleThreadExecutor();

    OutputSource<String, TestContext> defaultOutput = Mockito.mock(OutputSource.class);
    OutputSource<String, TestContext2> contextOutput = Mockito.mock(OutputSource.class);
    OutputSource<String, TestContext> context2Output = Mockito.mock(OutputSource.class);

    var task = MeshineryTask.<String, TestContext>builder()
        .inputSource(mockInputSource)
        .defaultOutputSource(defaultOutput)
        .read(INPUT_KEY, executor)
        .process(processorA)
        .write(INPUT_KEY)
        .contextSwitch(contextOutput, this::map)
        .process(processorB)
        .write(INPUT_KEY)
        .contextSwitch(context2Output, this::map)
        .process(processorA)
        .write(INPUT_KEY);

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .build();

    var batchJobFinished = executor.awaitTermination(10, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();

    Mockito.verify(mockInputSource, times(2)).getInputs(eq(INPUT_KEY));
    Mockito.verify(processorA, times(2)).processAsync(any(), any());
    Mockito.verify(processorB).processAsync(any(), any());
    Mockito.verify(defaultOutput).writeOutput(eq(INPUT_KEY), any());
    Mockito.verify(contextOutput).writeOutput(eq(INPUT_KEY), any());
    Mockito.verify(context2Output).writeOutput(eq(INPUT_KEY), eq(EXPECTED));
  }
}
