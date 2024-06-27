package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.processors.ParallelProcessor;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.AbstractTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ComplexParallelizationTest extends AbstractTestBase {

  @Test
  @SuppressWarnings("unchecked")
  void testComplexParallelization() throws InterruptedException {
    //Arrange ---------------------------------------------------------------------------------
    try (var executor = Executors.newFixedThreadPool(3)) {
      var inputSource = TestInputSource.builder()
          .todo(new TestContext(0))
          .build();

      MeshinerySourceConnector<String, TestContext> outputMock = Mockito.mock(MeshinerySourceConnector.class);

      var task = MeshineryTaskFactory.<String, TestContext>builder()
          .read("Test")
          .inputSource(inputSource)
          .outputSource(outputMock)
          .process(ParallelProcessor.<TestContext>builder()
              .parallel(new TestContextProcessor(30))
              .parallel(new TestContextProcessor(30))
              .parallel(new TestContextProcessor(30))
              .combine(this::getCombine))
          .write("")
          .build();

      //Act -------------------------------------------------------------------------------------
      RoundRobinScheduler.builder()
          .isBatchJob(true)
          .task(task)
          .gracePeriodMilliseconds(0)
          .build()
          .start();
      var batchJobFinished = executor.awaitTermination(3000, TimeUnit.MILLISECONDS);

      //Assert ----------------------------------------------------------------------------------
      var argumentCapture = ArgumentCaptor.forClass(TestContext.class);
      Mockito.verify(outputMock).writeOutput(eq(""), argumentCapture.capture(), any());
      assertThat(batchJobFinished).isTrue();
      assertThat(argumentCapture.getValue())
          .extracting(TestContext::getIndex)
          .isEqualTo(93);
    }
  }
}
