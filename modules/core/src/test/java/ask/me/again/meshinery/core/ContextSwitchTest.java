package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.*;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

class ContextSwitchTest {

  private static final String INPUT_KEY = "Test";

  private static final TestContext EXPECTED = TestContext.builder()
      .id("string: 1")
      .build();

  @Test
  void contextSwitchTest() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var mockInputSource = Mockito.spy(new TestInputSource());

    var processorA = Mockito.spy(new TestProcessorA());
    var processorB = Mockito.spy(new TestProcessorB());

    var executor = Executors.newSingleThreadExecutor();

    OutputSource<String, Context> outputSource = Mockito.mock(OutputSource.class);

    var task = new MeshineryTask<String, Context, Context>()
        .inputSource(mockInputSource)
        .outputSource(outputSource)
        .read(INPUT_KEY, executor)
        .process(processorA)
        .process(processorB)
        .write("");

    //Act ------------------------------------------------------------------------------------
    new RoundRobinScheduler<>(true, List.of(task)).start();
    executor.awaitTermination(3000, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    Mockito.verify(mockInputSource, times(2)).getInputs(eq(INPUT_KEY));
    Mockito.verify(processorA).processAsync(any(), any());
    Mockito.verify(processorB).processAsync(any(), any());
    Mockito.verify(outputSource).writeOutput("", EXPECTED);
  }

  private static class TestInputSource implements InputSource<String, Context> {

    private int counter = 2;

    @Override
    public List<Context> getInputs(String key) {

      counter--;

      if (counter == 0) {
        return Collections.emptyList();
      }

      return List.of(TestContext.builder()
          .id(counter + "")
          .build());
    }
  }

  private static class TestProcessorA implements MeshineryProcessor<Context, String> {
    @Override
    public CompletableFuture<String> processAsync(Context context, Executor executor) {
      return CompletableFuture.completedFuture(context.getId());
    }
  }

  private static class TestProcessorB implements MeshineryProcessor<String, Context> {
    @Override
    public CompletableFuture<Context> processAsync(String context, Executor executor) {
      return CompletableFuture.completedFuture(TestContext.builder()
          .id("string: " + context)
          .build());
    }
  }

  @Value
  @Builder
  private static class TestContext implements Context {
    String id;
  }
}
