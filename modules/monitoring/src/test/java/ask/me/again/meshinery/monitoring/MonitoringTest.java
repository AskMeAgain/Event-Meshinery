package ask.me.again.meshinery.monitoring;

import ask.me.again.meshinery.core.common.DataInjectingExecutorService;
import ask.me.again.meshinery.core.task.TaskData;
import ask.me.again.meshinery.core.utils.context.TestContext;
import ask.me.again.meshinery.core.utils.processor.TestContextProcessor;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static ask.me.again.meshinery.core.task.TaskDataProperties.TASK_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class MonitoringTest {

  public static final String TASK_NAME_VALUE = "test";

  @Test
  void testMonitoringDecorator() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    TaskData.setTaskData(new TaskData().put(TASK_NAME, TASK_NAME_VALUE));
    MDC.put("","");
    var decorator = new TimingDecorator<TestContext, TestContext>();
    var simpleProcessor = new TestContextProcessor(0);
    var decoratedProcessor = decorator.wrap(simpleProcessor);

    //Act --------------------------------------------------------------------------------------------------------------
    var context = new TestContext(1);
    var executor = new DataInjectingExecutorService(Executors.newSingleThreadExecutor());
    decoratedProcessor.processAsync(context, executor);
    assertThat(MeshineryMonitoringService.inProcessingGauge.labels(TASK_NAME_VALUE).get()).isEqualTo(1);

    //Assert -----------------------------------------------------------------------------------------------------------
  }

}
