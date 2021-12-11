package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.processors.BranchProcessor;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class BranchTest {

  private static final String KEY = "Test";

  @ParameterizedTest
  @CsvSource({"true,false,1, false", "false,true,2,true"})
  void testBranching(boolean firstCondition, boolean secondCondition, int expected, boolean expectSecondProcessorToRun)
      throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .iterations(1)
        .build();
    var executor = Executors.newSingleThreadExecutor();
    var spyProcessor = Mockito.spy(new TestContextProcessor(2));
    MeshineryConnector<String, TestContext> defaultOutputSource = Mockito.mock(MeshineryConnector.class);

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .defaultOutputSource(defaultOutputSource)
        .read(KEY, executor)
        .process(BranchProcessor.<TestContext>builder()
            .branch(new TestContextProcessor(1), x -> firstCondition)
            .branch(spyProcessor, x -> secondCondition))
        .write("Test")
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(1500, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(defaultOutputSource).writeOutput(any(), eq(new TestContext(expected)));
    if (expectSecondProcessorToRun) {
      Mockito.verify(spyProcessor).processAsync(any(), any());
    }
  }
}
