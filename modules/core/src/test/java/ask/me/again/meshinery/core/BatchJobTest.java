package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.mockito.ArgumentMatchers.eq;

class BatchJobTest {

  public static final String INPUT_KEY = "Test";
  public static final int ITERATIONS = 4;

  @Test
  void testBatchJobFlag() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var mockInputSource = Mockito.spy(new TestInputSource());

    ExecutorService executor = Executors.newSingleThreadExecutor();
    var task = MeshineryTask.<String, Context>builder()
      .inputSource(mockInputSource)
      .read(INPUT_KEY, executor)
      .build();

    //Act ------------------------------------------------------------------------------------
    new RoundRobinScheduler(List.of(task), true).start();

    //Assert ---------------------------------------------------------------------------------
    executor.awaitTermination(3, TimeUnit.SECONDS);
    Mockito.verify(mockInputSource, Mockito.times(ITERATIONS)).getInputs(eq(INPUT_KEY));
  }

  private static class TestInputSource implements InputSource<String, Context> {

    private int counter = ITERATIONS;

    @Override
    public List<Context> getInputs(String key) {
      System.out.println("iteration: " + counter);
      counter--;

      if (counter == 0) {
        return Collections.emptyList();
      }

      return List.of(() -> counter + "");
    }
  }

  private static ExecutorService currentThreadExecutorService() {
    ThreadPoolExecutor.CallerRunsPolicy callerRunsPolicy = new ThreadPoolExecutor.CallerRunsPolicy();
    return new ThreadPoolExecutor(0, 1, 0L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), callerRunsPolicy) {
      @Override
      public void execute(Runnable command) {
        callerRunsPolicy.rejectedExecution(command, this);
      }
    };
  }
}
