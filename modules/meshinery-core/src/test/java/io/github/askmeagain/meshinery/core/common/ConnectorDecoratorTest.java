package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.decorators.TestInputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class ConnectorDecoratorTest {

  @Test
  void decoraterTest() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var connector = new MemoryConnector<String, TestContext>();
    connector.writeOutput("Abc", TestContext.builder().id("A").build());

    var inputCounter = new AtomicInteger();
    var executor = Executors.newSingleThreadExecutor();

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .taskName("Test")
        .read(executor, "Abc")
        .connector(connector)
        .process(new TestContextProcessor(1))
        .write("Def", "def2")
        .build();

    var spyDecorator = Mockito.spy(new TestInputSourceDecoratorFactory(inputCounter));

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .task(task)
        .registerConnectorDecorators(List.of(spyDecorator))
        .isBatchJob(true)
        .gracePeriodMilliseconds(500)
        .buildAndStart();

    var batchJobFinished = executor.awaitTermination(4000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(spyDecorator, Mockito.atLeastOnce()).decorate(any());
    assertThat(inputCounter).hasValueGreaterThan(1);
  }


}
