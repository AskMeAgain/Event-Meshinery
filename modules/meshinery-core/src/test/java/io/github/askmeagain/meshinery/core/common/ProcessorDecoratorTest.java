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
    MeshineryConnector<String, TestContext> mockOutputSource = Mockito.mock(MeshineryConnector.class);

    var decorator = new ProcessorDecorator<>() {
      @Override
      public MeshineryProcessor<DataContext, DataContext> wrap(
          MeshineryProcessor<DataContext, DataContext> processor
      ) {

        return (c, executor) -> {
          var context = (TestContext) c;
          return processor.processAsync(context.toBuilder()
              .index(context.getIndex() + 1)
              .build(), executor);
        };
      }
    };

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .defaultOutputSource(mockOutputSource)
        .read("", executor)
        .process(new TestContextProcessor(1))
        .write("")
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .registerDecorators(List.of(decorator))
        .gracePeriod(0)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(2, TimeUnit.SECONDS);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(mockOutputSource).writeOutput(any(), eq(TestContext.builder()
        .id("2")
        .index(3)
        .build()));

  }

  @SneakyThrows
  @Test
  void testTaskDecorator() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();
    var executor = Executors.newSingleThreadExecutor();
    MeshineryConnector<String, TestContext> mockOutputSource = Mockito.mock(MeshineryConnector.class);

    var decorator = new ProcessorDecorator<TestContext, TestContext>() {
      @Override
      public MeshineryProcessor<TestContext, TestContext> wrap(
          MeshineryProcessor<TestContext, TestContext> processor
      ) {
        return (context, executor) -> processor.processAsync(context.toBuilder()
            .index(context.getIndex() + 1)
            .build(), executor);
      }
    };

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .defaultOutputSource(mockOutputSource)
        .read("", executor)
        .registerDecorator(decorator)
        .process(new TestContextProcessor(1))
        .write("")
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .gracePeriod(0)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(2, TimeUnit.SECONDS);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(mockOutputSource).writeOutput(any(), eq(TestContext.builder()
        .id("2")
        .index(2)
        .build()));

  }

}
