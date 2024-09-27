package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class InputSourceDecoratorTest {


  @SneakyThrows
  @Test
  void testInputDecorator() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();
    var executor = Executors.newSingleThreadExecutor();
    MeshinerySourceConnector<String, TestContext> mockOutputSource = Mockito.mock(MeshinerySourceConnector.class);

    var calledInputs = new ArrayList<String>();
    var decorator = new InputSourceDecoratorFactory<String, TestContext>() {
      @Override
      public MeshineryInputSource<String, TestContext> decorate(MeshineryInputSource<String, TestContext> connector) {
        return new MeshineryInputSource<>() {
          @Override
          public String getName() {
            return connector.getName();
          }

          @Override
          public List<TestContext> getInputs(List<String> key) {
            calledInputs.addAll(key);
            return connector.getInputs(key);
          }

          @Override
          public TestContext commit(TestContext context) {
            return connector.commit(context);
          }
        };
      }
    };

    var task = MeshineryTask.<String, TestContext>builder()
        .inputSource(inputSource)
        .outputSource(mockOutputSource)
        .read("input")
        .process(new TestContextProcessor(1))
        .registerInputSourceDecorator(decorator)
        .write("")
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
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
        .id("1")
        .index(1)
        .build()), any());
    assertThat(calledInputs).contains("input");

  }
}