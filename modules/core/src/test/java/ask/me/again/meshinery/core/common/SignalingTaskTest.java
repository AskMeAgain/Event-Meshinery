package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.source.MemoryConnector;
import ask.me.again.meshinery.core.task.MeshineryTaskFactory;
import ask.me.again.meshinery.core.utils.context.TestContext;
import ask.me.again.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class SignalingTaskTest {

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
    OutputSource<String, TestContext> outputSource = Mockito.spy(memoryInputSource);

    memoryInputSource.writeOutput("Ignored", new TestContext(0));
    memoryInputSource.writeOutput("Test", new TestContext(1));

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .read("", executor)
        .defaultOutputSource(outputSource)
        .readNewInput("Test", memoryInputSource)
        .write("")
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(1500, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    Mockito.verify(outputSource).writeOutput(any(), eq(new TestContext(1)));
    Mockito.verify(outputSource, Mockito.never()).writeOutput(any(), eq(new TestContext(0)));
  }

}