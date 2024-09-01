package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.monitoring.decorators.ProcessorTimingDecorator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class MonitoringTest {

  private static final String TASK_NAME_VALUE = "test";

  @Test
  void testMonitoringDecorator() throws ExecutionException, InterruptedException {
    //Arrange ----------------------------------------------------------------------------------------------------------
    TaskData.setTaskData(new TaskData().with(TASK_NAME, TASK_NAME_VALUE));
    MDC.put("", "");
    var decorator = new ProcessorTimingDecorator<TestContext>();
    var decoratedProcessor = decorator.wrap(this::wait);

    //Act --------------------------------------------------------------------------------------------------------------
    var context = new TestContext(1);

    var future = CompletableFuture.runAsync(() -> decoratedProcessor.process(context));

    Thread.sleep(100);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(MeshineryMonitoringService.IN_PROCESSING_COUNTER.labels(TASK_NAME_VALUE).get()).isEqualTo(1);

    future.get();
  }

  @SneakyThrows
  private TestContext wait(TestContext context) {
    Thread.sleep(1000);
    return context;
  }

}
