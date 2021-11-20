package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.core.task.MeshineryTaskFactory;
import ask.me.again.meshinery.core.task.TaskReplayFactory;
import ask.me.again.meshinery.core.utils.context.TestContext;
import ask.me.again.meshinery.core.utils.processor.TestContextProcessor;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class TaskReplayFactoryTest {

  @Test
  void testInputFactory() throws ExecutionException, InterruptedException {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var processorA = Mockito.spy(new TestContextProcessor(1));
    var processorB = Mockito.spy(new TestContextProcessor(2));
    var testDataProcessor = Mockito.spy(new TaskDataProcessor());

    OutputSource<String, TestContext> outputSource = Mockito.mock(OutputSource.class);

    List<MeshineryTask<?, ? extends Context>> tasks = List.of(
        MeshineryTaskFactory.<String, TestContext>builder()
            .defaultOutputSource(outputSource)
            .taskName("test")
            .process(processorA)
            .process(processorB)
            .process(testDataProcessor)
            .write("OutputKey")
            .putData("test", "1234")
            .build(),
        MeshineryTaskFactory.<String, TestContext>builder()
            .taskName("test2")
            .build()
    );
    var executor = Executors.newSingleThreadExecutor();
    var taskReplayFactory = new TaskReplayFactory(tasks, executor);

    //Act --------------------------------------------------------------------------------------------------------------
    taskReplayFactory.replay("test", new TestContext(3));

    //Assert -----------------------------------------------------------------------------------------------------------
    Mockito.verify(processorA).processAsync(any(), any());
    Mockito.verify(processorB).processAsync(any(), any());
    Mockito.verify(outputSource).writeOutput(eq("OutputKey"), eq(new TestContext(61234)));

  }

  private static class TaskDataProcessor implements MeshineryProcessor<TestContext, TestContext> {

    @Override
    public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
      return CompletableFuture.completedFuture(context.toBuilder()
          .id(context.getIndex() + getTaskData().getSingle("test"))
          .index(Integer.parseInt(context.getIndex() + getTaskData().getSingle("test")))
          .build());
    }
  }

}