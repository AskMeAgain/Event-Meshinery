package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.monitoring.decorators.ProcessorTimingDecorator;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class MonitoringTest {

  private static final String TASK_NAME_VALUE = "test";

  @Test
  void testMonitoringDecorator() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    TaskData.setTaskData(new TaskData().put(TASK_NAME, TASK_NAME_VALUE));
    MDC.put("", "");
    var decorator = new ProcessorTimingDecorator<TestContext, TestContext>();
    var simpleProcessor = new TestContextProcessor(0);
    var decoratedProcessor = decorator.wrap(simpleProcessor);

    //Act --------------------------------------------------------------------------------------------------------------
    var context = new TestContext(1);
    var executor = new DataInjectingExecutorService(TASK_NAME_VALUE, Executors.newSingleThreadExecutor());
    decoratedProcessor.processAsync(context, executor);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(MeshineryMonitoringService.IN_PROCESSING_COUNTER.labels(TASK_NAME_VALUE).get()).isEqualTo(1);
  }

}
