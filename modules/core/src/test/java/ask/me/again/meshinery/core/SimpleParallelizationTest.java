package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.processors.ParallelProcessor;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.eq;

public class SimpleParallelizationTest {

  @Test
  void testSimpleParallelization() throws InterruptedException {
    //Arrange ---------------------------------------------------------------------------------
    var executor = Executors.newFixedThreadPool(3);

    var mock = Mockito.spy(new TestContext(0));
    var task = MeshineryTask.<String, TestContext>builder()
        .read("Test", executor)
        .inputSource(new TestInputSource())
        .process(ParallelProcessor.<TestContext>builder()
            .parallel(new TestProcessor(3))
            .parallel(new TestProcessor(3))
            .combine(getCombine(mock)));

    //Act -------------------------------------------------------------------------------------
    new RoundRobinScheduler<>(true, List.of(task)).start();
    executor.awaitTermination(5, TimeUnit.SECONDS);

    //Assert ----------------------------------------------------------------------------------
    Mockito.verify(mock).setId(eq(6));
  }

  private Function<List<TestContext>, TestContext> getCombine(TestContext result) {
    return list -> {
      var sum = list.stream().mapToInt(TestContext::getIdAsInt).sum();
      result.setId(sum);
      return result;
    };
  }

  private static class TestInputSource implements InputSource<String, TestContext> {

    private int counter = 2;

    @Override
    public List<TestContext> getInputs(String key) {

      counter--;

      if (counter == 0) {
        return Collections.emptyList();
      }

      return List.of(new TestContext(3));
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
        return context;
      }, executor);
    }
  }

  private static class TestContext implements Context {

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
}
