package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.AbstractTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.context.TestContext2;
import io.github.askmeagain.meshinery.core.utils.processor.TestContext2Processor;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.Collections;
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

    MeshineryConnector<String, TestContext> defaultOutput = Mockito.mock(MeshineryConnector.class);
    MeshineryConnector<String, TestContext2> contextOutput = Mockito.mock(MeshineryConnector.class);
    MeshineryConnector<String, TestContext> context2Output = Mockito.mock(MeshineryConnector.class);

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(mockInputSource)
        .defaultOutputSource(defaultOutput)
        .read(INPUT_KEY, executor)
        .process(processorA)
        .write(INPUT_KEY)
        .contextSwitch(contextOutput, this::map, Collections.emptyList())
        .process(processorB)
        .write(INPUT_KEY)
        .contextSwitch(context2Output, this::map, Collections.emptyList())
        .process(processorA)
        .write(INPUT_KEY)
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .buildAndStart();

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
