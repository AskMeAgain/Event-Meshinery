package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import io.github.askmeagain.meshinery.core.utils.sources.TestOutputSource;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;

class PriorityQueueTest {

  @Test
  void testPriorityQueue() throws InterruptedException {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var properties = new MeshineryCoreProperties();
    properties.setBatchJob(true);
    properties.setBackpressureLimit(50);

    var received = new ConcurrentSkipListSet<Integer>();

    var executor = Executors.newFixedThreadPool(10);

    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .iterations(10000)
        .build();

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .outputSource(new TestOutputSource())
        .read(executor, "")
        .process((ctx, e) -> CompletableFuture.supplyAsync(() -> {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
              }
              received.add(Integer.parseInt(ctx.getId()));
              return ctx;
            }
            , e))
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .properties(properties)
        .task(task)
        .backpressureLimit(10000)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(1500, TimeUnit.MILLISECONDS);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(batchJobFinished).isFalse(); //here we needed to stop prematurely

    assertThat(received).contains(1,2,3,4,5,6,7,8,9,10);
  }
}
