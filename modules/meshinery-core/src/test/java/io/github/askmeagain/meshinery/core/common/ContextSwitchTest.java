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
    var inputSource = TestInputSource.builder()
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
        .outputSource(defaultOutput)
        .read(executor, INPUT_KEY)
        .process(processorA)
        .write(INPUT_KEY + "asd")
        .contextSwitch(contextOutput, this::map)
        .process(processorB)
        .write(INPUT_KEY + "asd")
        .contextSwitch(context2Output, this::map)
        .process(processorA)
        .write(INPUT_KEY + "asd")
        .backoffTime(100)
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .gracePeriodMilliseconds(0)
        .buildAndStart();

    var batchJobFinished = executor.awaitTermination(4000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();

    Mockito.verify(processorA, times(2)).processAsync(any(), any());
    Mockito.verify(processorB).processAsync(any(), any());
    Mockito.verify(defaultOutput).writeOutput(eq(INPUT_KEY + "asd"), any());
    Mockito.verify(contextOutput).writeOutput(eq(INPUT_KEY + "asd"), any());
    Mockito.verify(context2Output).writeOutput(eq(INPUT_KEY + "asd"), eq(EXPECTED));
  }
}
