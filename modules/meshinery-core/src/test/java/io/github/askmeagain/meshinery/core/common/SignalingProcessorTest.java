package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class SignalingProcessorTest {

  @Test
  @SneakyThrows
  void signalingTest() {
    //Arrange --------------------------------------------------------------------------------
    var executor = Executors.newSingleThreadExecutor();
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .iterations(4)
        .build();
    var memoryInputSource = new MemoryConnector<String, TestContext>();
    MemoryConnector<String, TestContext> outputSource = Mockito.spy(memoryInputSource);

    memoryInputSource.writeOutput("Ignored", new TestContext(0));
    memoryInputSource.writeOutput("Test", new TestContext(1));

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .read(executor, "")
        .defaultOutputSource(outputSource)
        .readNewInput("Test", memoryInputSource)
        .write("")
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .gracePeriodMilliseconds(0)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(1500, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(outputSource).writeOutput(any(), eq(new TestContext(1)));
    Mockito.verify(outputSource, Mockito.never()).writeOutput(any(), eq(new TestContext(0)));
  }

}
