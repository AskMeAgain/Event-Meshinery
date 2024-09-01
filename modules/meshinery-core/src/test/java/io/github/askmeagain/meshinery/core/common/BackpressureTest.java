package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import io.github.askmeagain.meshinery.core.utils.sources.TestOutputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

class BackpressureTest {

  @Test
  void testBackpressure() throws InterruptedException {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var properties = new MeshineryCoreProperties();
    properties.setBatchJob(true);
    properties.setBackpressureLimit(10);

    var executor = Executors.newFixedThreadPool(10);
    var processor = Mockito.spy(new TestContextProcessor(0));

    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .iterations(100)
        .build();

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .outputSource(new TestOutputSource())
        .read("")
        .process(processor)
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .properties(properties)
        .task(task)
        .executorService(executor)
        .build()
        .start();
    var batchJobFinished = executor.awaitTermination(500, TimeUnit.MILLISECONDS);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(batchJobFinished).isFalse(); //here we needed to stop prematurely
    Mockito.verify(processor, times(10)).process(any());
  }
}
