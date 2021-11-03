package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.common.RoundRobinScheduler;
import ask.me.again.meshinery.core.common.context.TestContext;
import ask.me.again.meshinery.core.common.processor.TestContextProcessor;
import ask.me.again.meshinery.core.common.sources.TestInputSource;
import ask.me.again.meshinery.core.processors.BranchProcessor;
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
    OutputSource<String, TestContext> defaultOutputSource = Mockito.mock(OutputSource.class);

    var task = MeshineryTask.<String, TestContext>builder()
        .inputSource(inputSource)
        .defaultOutputSource(defaultOutputSource)
        .read(KEY, executor)
        .process(BranchProcessor.<TestContext>builder()
            .branch(new TestContextProcessor(1), x -> firstCondition)
            .branch(spyProcessor, x -> secondCondition))
        .write("Test");

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .build();
    var batchJobFinished = executor.awaitTermination(1500, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(defaultOutputSource).writeOutput(any(), eq(new TestContext(expected)));
    if (expectSecondProcessorToRun) {
      Mockito.verify(spyProcessor).processAsync(any(), any());
    }
  }
}
