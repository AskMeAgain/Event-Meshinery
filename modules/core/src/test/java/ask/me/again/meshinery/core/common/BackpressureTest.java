package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.core.utils.context.TestContext;
import ask.me.again.meshinery.core.utils.processor.TestContextProcessor;
import ask.me.again.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;

class BackpressureTest {

  @Test
  void testBackpressure() throws InterruptedException {

    //Arrange ----------------------------------------------------------------------------------------------------------
    var executor = Executors.newFixedThreadPool(11);
    var processor = Mockito.spy(new TestContextProcessor(0));
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .iterations(100)
        .build();

    var task = MeshineryTask.<String, TestContext>builder()
        .inputSource(inputSource)
        .read("", executor)
        .process(processor);

    //Act --------------------------------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .backpressureLimit(10)
        .task(task)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(500, TimeUnit.MILLISECONDS);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(batchJobFinished).isFalse(); //here we needed to stop prematurely
    //the backpressure will be matched +-1 as the scheduler pushes/pops to the queue continuously
    Mockito.verify(processor, atMost(11)).processAsync(any(), any());
  }
}
