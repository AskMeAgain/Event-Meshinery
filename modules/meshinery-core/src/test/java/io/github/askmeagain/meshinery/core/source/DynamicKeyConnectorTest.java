package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class DynamicKeyConnectorTest {

  @Test
  void getInputs() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var innerConnector = Mockito.spy(new MemoryConnector<String, TestContext>());
    MeshineryOutputSource<String, TestContext> innerOutputMock = Mockito.mock(MeshineryOutputSource.class);

    var executor = Executors.newSingleThreadExecutor();
    var dynamicConnector = DynamicKeyConnector.<String, TestContext>builder()
        .name("abc")
        .keySupplier(ctx -> List.of(ctx.getId()))
        .outerInputSource(innerConnector)
        .innerInputSource(innerConnector)
        .innerOutputSource(innerOutputMock)
        .build();

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .connector(dynamicConnector)
        .read("outer key")
        .write("result")
        .build();

    var trigger = TestContext.builder()
        .id("inner key")
        .build();

    var innerContext = TestContext.builder()
        .id("inner context")
        .build();

    innerConnector.writeOutput("outer key", trigger, new TaskData());
    innerConnector.writeOutput("inner key", innerContext, new TaskData());

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.builder()
        .isBatchJob(true)
        .task(task)
        .executorService(executor)
        .gracePeriodMilliseconds(1000)
        .buildAndStart();

    var batchJobFinished = executor.awaitTermination(2000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    Mockito.verify(innerOutputMock).writeOutput(eq("result"), eq(innerContext), any());
  }
}