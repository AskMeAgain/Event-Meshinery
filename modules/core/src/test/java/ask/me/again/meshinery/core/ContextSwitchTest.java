package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.*;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.TestContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

class ContextSwitchTest {

  private static final String INPUT_KEY = "Test";

  private static final TestContext1 EXPECTED = TestContext1.builder()
      .id("end: 1")
      .build();

  @Test
  @SuppressWarnings("unchecked")
  void contextSwitchTest() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var mockInputSource = Mockito.spy(new TestInputSource());

    var processorA = Mockito.spy(new TestProcessorA());
    var processorB = Mockito.spy(new TestProcessorB());

    var executor = Executors.newSingleThreadExecutor();

    OutputSource<String, TestContext1> outputSource = Mockito.mock(OutputSource.class);

    var task = new MeshineryTask<String, TestContext1>()
        .inputSource(mockInputSource)
        .defaultOutputSource(outputSource)
        .read(INPUT_KEY, executor)
        .process(processorA)
        .contextSwitch(getMap())
        .process(processorB)
        .write("");

    //Act ------------------------------------------------------------------------------------
    new RoundRobinScheduler<>(true, List.of(task)).start();
    executor.awaitTermination(3, TimeUnit.SECONDS);

    //Assert ---------------------------------------------------------------------------------
    Mockito.verify(mockInputSource, times(2)).getInputs(eq(INPUT_KEY));
    Mockito.verify(processorA).processAsync(any(), any());
    Mockito.verify(processorB).processAsync(any(), any());
    Mockito.verify(outputSource).writeOutput(eq(""), eq(EXPECTED));
  }

  private Function<TestContext1, TestContext2> getMap() {
    return null;
  }

  private static class TestInputSource implements InputSource<String, TestContext1> {

    private int counter = 2;

    @Override
    public List<TestContext1> getInputs(String key) {

      counter--;

      if (counter == 0) {
        return Collections.emptyList();
      }

      return List.of(TestContext1.builder()
          .id(counter + "")
          .build());
    }
  }

  private static class TestProcessorA implements MeshineryProcessor<TestContext1, TestContext1> {
    @Override
    public CompletableFuture<TestContext1> processAsync(TestContext1 context, Executor executor) {
      return CompletableFuture.completedFuture(TestContext1.builder()
          .id(context.getId())
          .build());
    }
  }

  private static class TestProcessorB implements MeshineryProcessor<TestContext2, TestContext2> {
    @Override
    public CompletableFuture<TestContext2> processAsync(TestContext2 context, Executor executor) {
      return CompletableFuture.completedFuture(TestContext2.builder()
          .id("end: " + context.getId())
          .build());
    }
  }

  @Value
  @Builder
  private static class TestContext1 implements Context {
    String id;
    Integer a;
  }

  @Value
  @Builder
  private static class TestContext2 implements Context {
    String id;
    Integer b;
  }
}
