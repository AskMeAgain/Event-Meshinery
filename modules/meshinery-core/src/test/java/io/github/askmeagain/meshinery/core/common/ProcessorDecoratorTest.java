package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ProcessorDecoratorTest {

  @SneakyThrows
  @Test
  void testDecorator() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();
    var executor = Executors.newSingleThreadExecutor();
    MeshinerySourceConnector<String, TestContext> mockOutputSource = Mockito.mock(MeshinerySourceConnector.class);

    var decorator = new ProcessorDecorator<TestContext>() {
      @Override
      public MeshineryProcessor<TestContext, TestContext> wrap(
          MeshineryProcessor<TestContext, TestContext> processor
      ) {
        return (TestContext c) -> processor.process(c.toBuilder()
            .index(c.getIndex() + 1)
            .build());
      }
    };

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .outputSource(mockOutputSource)
        .read("")
        .process(new TestContextProcessor(1))
        .write("")
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .batchJob(true)
        .task(task)
        .executorService(executor)
        .registerProcessorDecorators(List.of(decorator))
        .gracePeriodMilliseconds(0)
        .build()
        .start();
    var batchJobFinished = executor.awaitTermination(2, TimeUnit.SECONDS);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(mockOutputSource).writeOutput(any(), eq(TestContext.builder()
        .id("2")
        .index(3)
        .build()), any());

  }

  @SneakyThrows
  @Test
  void testTaskDecorator() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();
    var executor = Executors.newSingleThreadExecutor();
    MeshinerySourceConnector<String, TestContext> mockOutputSource = Mockito.mock(MeshinerySourceConnector.class);

    var decorator = new ProcessorDecorator<TestContext>() {
      @Override
      public MeshineryProcessor<TestContext, TestContext> wrap(
          MeshineryProcessor<TestContext, TestContext> processor
      ) {
        return (context) -> processor.process(context.toBuilder()
            .index(context.getIndex() + 1)
            .build());
      }
    };

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .outputSource(mockOutputSource)
        .read("")
        .registerProcessorDecorator(decorator)
        .process(new TestContextProcessor(1))
        .write("")
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .batchJob(true)
        .task(task)
        .executorService(executor)
        .gracePeriodMilliseconds(0)
        .build()
        .start();
    var batchJobFinished = executor.awaitTermination(2, TimeUnit.SECONDS);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(mockOutputSource).writeOutput(any(), eq(TestContext.builder()
        .id("2")
        .index(2)
        .build()), any());

  }

}
