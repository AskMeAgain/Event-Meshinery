package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class WriteTest {

  private static final String KEY = "Test";

  @Test
  void writeIfTest() {
    //Arrange --------------------------------------------------------------------------------
    var memoryConnector = Mockito.spy(new MemoryConnector<String, TestContext>());
    var context = new TestContext(0);

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .connector(memoryConnector)
        .read(Executors.newSingleThreadExecutor(), "input")
        .write("abc", c -> c.getId().equals("1"))
        .write("abc2", c -> c.getId().equals("0"))
        .build();


    //Act ------------------------------------------------------------------------------------
    memoryConnector.writeOutput("input", context);

    RoundRobinScheduler.builder()
        .gracePeriodMilliseconds(100)
        .task(task)
        .isBatchJob(true)
        .buildAndStart();

    //Assert ---------------------------------------------------------------------------------
    Mockito.verify(memoryConnector).writeOutput(eq("abc2"), eq(context));
    Mockito.verify(memoryConnector, Mockito.never()).writeOutput(eq("abc"), eq(context));
  }

  @Test
  @SuppressWarnings("unchecked")
  void writeTest() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();

    var mockInputSource = Mockito.spy(inputSource);
    MeshineryConnector<String, TestContext> mockOutputSource = Mockito.mock(MeshineryConnector.class);
    MeshineryConnector<String, TestContext> defaultOutputSource = Mockito.mock(MeshineryConnector.class);
    var executor = Executors.newSingleThreadExecutor();

    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(mockInputSource)
        .outputSource(defaultOutputSource)
        .read(executor, KEY)
        .write(KEY, mockOutputSource)
        .write(KEY, KEY)
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .gracePeriodMilliseconds(0)
        .buildAndStart();
    var batchJobFinished = executor.awaitTermination(1, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();

    Mockito.verify(mockOutputSource).writeOutput(any(), any());
    Mockito.verify(defaultOutputSource, Mockito.times(2)).writeOutput(any(), any());
  }
}
