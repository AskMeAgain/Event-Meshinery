package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.ErrorProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@Slf4j
class ExceptionHandlingTest {

  public static final String KEY = "Test";
  public static final int ITERATIONS = 2;

  private static final TestContext EXPECTED = TestContext.builder()
      .id("new Id")
      .build();

  @Test
  @SuppressWarnings("unchecked")
  void exceptionDefaultTest() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();

    var mockInputSource = Mockito.spy(inputSource);
    MeshinerySourceConnector<String, TestContext> mockOutputSource = Mockito.mock(MeshinerySourceConnector.class);
    var executor = Executors.newSingleThreadExecutor();

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(mockInputSource)
        .outputSource(mockOutputSource)
        .read(KEY)
        .process((context) -> {
          throw new RuntimeException("arg");
        })
        .write(KEY)
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .executorService(executor)
        .gracePeriodMilliseconds(0)
        .build()
        .start();

    var batchJobFinished = executor.awaitTermination(1, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();

    Mockito.verify(mockInputSource, Mockito.atLeast(ITERATIONS)).getInputs(eq(List.of(KEY)));
    Mockito.verify(mockOutputSource, Mockito.never()).writeOutput(any(), any(), any());
  }


  @Test
  @SuppressWarnings("unchecked")
  void exceptionOverrideTest() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();

    var mockInputSource = Mockito.spy(inputSource);
    MeshinerySourceConnector<String, TestContext> mockOutputSource = Mockito.mock(MeshinerySourceConnector.class);
    var executor = Executors.newSingleThreadExecutor();

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(mockInputSource)
        .outputSource(mockOutputSource)
        .read(KEY)
        .process(new ErrorProcessor())
        .exceptionHandler((ctx, exc) -> {
          log.info("Error Handling");
          return EXPECTED;
        })
        .write(KEY)
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .executorService(executor)
        .gracePeriodMilliseconds(0)
        .build()
        .start();

    var batchJobFinished = executor.awaitTermination(3, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();

    Mockito.verify(mockInputSource, Mockito.atLeast(ITERATIONS)).getInputs(eq(List.of(KEY)));
    Mockito.verify(mockOutputSource).writeOutput(any(), eq(EXPECTED), any());
  }
}
