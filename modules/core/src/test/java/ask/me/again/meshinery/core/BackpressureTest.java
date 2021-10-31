package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.RoundRobinScheduler;
import ask.me.again.meshinery.core.common.context.TestContext;
import ask.me.again.meshinery.core.common.processor.TestContextProcessor;
import ask.me.again.meshinery.core.common.sources.TestInputSource;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.Mockito;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;

public class BackpressureTest {

  @RepeatedTest(10)
  void testBackpressure() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
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

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .backpressureLimit(10)
        .task(task)
        .build();
    var batchJobFinished = executor.awaitTermination(500, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isFalse(); //here we needed to stop prematurely
    //the backpressure will be matched +-1 as the scheduler pushes/pops to the queue continuously
    Mockito.verify(processor, atMost(11)).processAsync(any(), any());
  }
}
