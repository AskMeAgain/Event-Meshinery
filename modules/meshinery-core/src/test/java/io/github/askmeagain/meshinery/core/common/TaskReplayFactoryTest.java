package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskNotFoundException;
import io.github.askmeagain.meshinery.core.task.TaskReplayFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TaskDataTestProcessor;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class TaskReplayFactoryTest {

  @Test
  void testInputFactory() throws ExecutionException, InterruptedException, MeshineryTaskNotFoundException {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var processorA = Mockito.spy(new TestContextProcessor(1));
    var processorB = Mockito.spy(new TestContextProcessor(2));
    var testDataProcessor = Mockito.spy(new TaskDataTestProcessor());

    MeshineryConnector<String, TestContext> outputSource = Mockito.mock(MeshineryConnector.class);

    List<MeshineryTask<?, ? extends DataContext>> tasks = List.of(
        MeshineryTaskFactory.<String, TestContext>builder()
            .defaultOutputSource(outputSource)
            .inputSource(new TestInputSource(Collections.emptyList(), 0, 0, 0))
            .read("", Executors.newSingleThreadExecutor())
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
            .read("", Executors.newSingleThreadExecutor())
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
}