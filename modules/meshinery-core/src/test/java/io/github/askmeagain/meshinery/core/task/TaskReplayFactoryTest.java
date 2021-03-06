package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TaskDataTestProcessor;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class TaskReplayFactoryTest {

  public static final String KEY = "key";
  public static final String TASK_2 = "task2";

  @Test
  void testInputFactory() throws ExecutionException, InterruptedException, MeshineryTaskNotFoundException {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var processorA = Mockito.spy(new TestContextProcessor(1));
    var processorB = Mockito.spy(new TestContextProcessor(2));
    var testDataProcessor = Mockito.spy(new TaskDataTestProcessor());

    MeshineryConnector<String, TestContext> outputSource = Mockito.mock(MeshineryConnector.class);

    List<MeshineryTask<?, ? extends DataContext>> tasks = List.of(
        MeshineryTaskFactory.<String, TestContext>builder()
            .outputSource(outputSource)
            .inputSource(new TestInputSource(Collections.emptyList(), 0, 0, 0))
            .read(Executors.newSingleThreadExecutor(), "")
            .taskName("test")
            .process(processorA)
            .process(processorB)
            .process(testDataProcessor)
            .write("OutputKey")
            .putData("test", "1234")
            .build(),
        MeshineryTaskFactory.<String, TestContext>builder()
            .taskName("test2")
            .inputSource(new TestInputSource(Collections.emptyList(), 0, 0, 0))
            .read(Executors.newSingleThreadExecutor(), "")
            .build()
    );
    var executor = Executors.newSingleThreadExecutor();
    var taskReplayFactory = new TaskReplayFactory(tasks, executor);

    //Act --------------------------------------------------------------------------------------------------------------
    taskReplayFactory.injectData("test", new TestContext(3));

    //Assert -----------------------------------------------------------------------------------------------------------
    Mockito.verify(processorA).processAsync(any(), any());
    Mockito.verify(processorB).processAsync(any(), any());
    Mockito.verify(outputSource).writeOutput(eq("OutputKey"), eq(new TestContext(61234)));

  }

  @Test
  @SneakyThrows
  void replayData() {
    //Arrange --------------------------------------------------------------------------------
    var memoryConnector = new MemoryConnector<String, TestContext>();
    var task1 = MeshineryTaskFactory.<String, TestContext>builder()
        .connector(memoryConnector)
        .read(null, KEY)
        .build();
    var task2 = MeshineryTaskFactory.<String, TestContext>builder()
        .connector(memoryConnector)
        .read(null, KEY)
        .taskName(TASK_2)
        .build();

    var taskReplayFactory = new TaskReplayFactory(List.of(task1, task2), Executors.newSingleThreadExecutor());
    var context = TestContext.builder()
        .id("123")
        .build();

    //Act ------------------------------------------------------------------------------------
    taskReplayFactory.replayData(TASK_2, context);

    var result = memoryConnector.getInputs(List.of(KEY));

    //Assert ---------------------------------------------------------------------------------
    assertThat(result).first()
        .isEqualTo(context);
  }
}