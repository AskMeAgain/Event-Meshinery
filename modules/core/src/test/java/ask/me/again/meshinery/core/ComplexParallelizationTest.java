package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.*;
import ask.me.again.meshinery.core.processors.ParallelProcessor;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

public class ComplexParallelizationTest {

  @Test
  void testComplexParallelization() throws InterruptedException {
    //Arrange ---------------------------------------------------------------------------------
    var executor = Executors.newFixedThreadPool(3);

    OutputSource<String, TestContext> outputMock = Mockito.mock(OutputSource.class);

    var task = MeshineryTask.<String, TestContext>builder()
        .read("Test", executor)
        .inputSource(new TestInputSource())
        .defaultOutputSource(outputMock)
        .process(ParallelProcessor.<TestContext>builder()
            .parallel(ListProcessor.<TestContext>builder()
                .process(new TestProcessorA(1))
                .process(new TestProcessorB(2)))
            .parallel(new TestProcessor(30))
            .parallel(new TestProcessor(30))
            .parallel(new TestProcessor(30))
            .combine(ComplexParallelizationTest::getCombine))
        .write("");

    //Act -------------------------------------------------------------------------------------
    new RoundRobinScheduler<>(true, List.of(task)).start();
    executor.awaitTermination(7, TimeUnit.SECONDS);

    //Assert ----------------------------------------------------------------------------------
    var argumentCapture = ArgumentCaptor.forClass(TestContext.class);
    Mockito.verify(outputMock).writeOutput(eq(""), argumentCapture.capture());
    assertThat(argumentCapture.getValue())
        .extracting(TestContext::getIndex)
        .isEqualTo(93);
  }

  private static TestContext getCombine(List<TestContext> list) {
    var sum = list.stream().mapToInt(TestContext::getIdAsInt).sum();
    return new TestContext(sum);
  }

  private static class TestInputSource implements InputSource<String, TestContext> {

    private int counter = 2;

    @Override
    public List<TestContext> getInputs(String key) {

      counter--;

      if (counter == 0) {
        return Collections.emptyList();
      }

      return List.of(new TestContext(0));
    }
  }

  private static class TestProcessor implements MeshineryProcessor<TestContext, TestContext> {

    private final int index;

    public TestProcessor(int i) {
      index = i;
    }

    @Override
    public CompletableFuture<TestContext> processAsync(TestContext context, Executor executor) {
      return CompletableFuture.supplyAsync(() -> {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return new TestContext(index + context.getIndex());
      }, executor);
    }
  }

  private static class TestProcessorA implements MeshineryProcessor<TestContext, TestContext2> {

    private final int index;

    public TestProcessorA(int i) {
      index = i;
    }

    @Override
    public CompletableFuture<TestContext2> processAsync(TestContext context, Executor executor) {
      return CompletableFuture.supplyAsync(() -> {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return new TestContext2(index + context.getIndex());
      }, executor);
    }
  }

  private static class TestProcessorB implements MeshineryProcessor<TestContext2, TestContext> {

    private final int index;

    public TestProcessorB(int i) {
      index = i;
    }

    @Override
    public CompletableFuture<TestContext> processAsync(TestContext2 context, Executor executor) {
      return CompletableFuture.supplyAsync(() -> {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        return new TestContext(index + context.getIndex());
      }, executor);
    }
  }

  private static class TestContext implements Context {

    @Getter
    private final int index;

    public TestContext(int index) {
      this.index = index;
    }

    public void setId(int id) {

    }

    @Override
    public String getId() {
      return null;
    }

    public int getIdAsInt() {
      return index;
    }
  }

  private static class TestContext2 implements Context {

    @Getter
    private final int index;

    public TestContext2(int index) {
      this.index = index;
    }

    public void setId(int id) {

    }

    @Override
    public String getId() {
      return null;
    }

    public int getIdAsInt() {
      return index;
    }
  }
}
