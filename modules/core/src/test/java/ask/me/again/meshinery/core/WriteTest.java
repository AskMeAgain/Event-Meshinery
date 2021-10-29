package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class WriteTest {

  public static final String KEY = "Test";
  public static final int ITERATIONS = 2;

  @Test
  void writeTest() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var mockInputSource = Mockito.spy(new TestInputSource());
    OutputSource<String, Context> mockOutputSource = Mockito.mock(OutputSource.class);
    OutputSource<String, Context> defaultOutputSource = Mockito.mock(OutputSource.class);
    var executor = Executors.newSingleThreadExecutor();

    var task = MeshineryTask.<String, Context>builder()
        .inputSource(mockInputSource)
        .defaultOutputSource(defaultOutputSource)
        .read(KEY, executor)
        .write(KEY, mockOutputSource)
        .write(KEY, KEY);

    //Act ------------------------------------------------------------------------------------
    new RoundRobinScheduler<>(true, List.of(task)).start();

    //Assert ---------------------------------------------------------------------------------
    executor.awaitTermination(3, TimeUnit.SECONDS);
    Mockito.verify(mockInputSource, Mockito.times(ITERATIONS)).getInputs(eq(KEY));
    Mockito.verify(mockOutputSource).writeOutput(any(), any());
    Mockito.verify(defaultOutputSource, Mockito.times(2)).writeOutput(any(), any());
  }

  private static class TestInputSource implements InputSource<String, Context> {

    private int counter = ITERATIONS;

    @Override
    public List<Context> getInputs(String key) {
      counter--;

      if (counter == 0) {
        return Collections.emptyList();
      }

      return List.of(() -> counter + "");
    }
  }

}
