package ask.me.again.meshinery.core;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.schedulers.RoundRobinScheduler;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.mockito.ArgumentMatchers.any;

class StopIfTest {

  public static final String INPUT_KEY = "Test";

  @Test
  void testStopIf() throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    InputSource<String, Context> mockInputSource = Mockito.mock(InputSource.class);
    var processor = new MeshineryProcessor<Context,Context>() {
      @Override
      public CompletableFuture<Context> processAsync(Context context, Executor executor) {
        return null;
      }
    };

    Mockito.when(mockInputSource.getInputs(any(String.class)))
        .thenReturn(List.of(TestContext.builder().id("1").build()), Collections.emptyList());

    var processorSpy = Mockito.spy(processor);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    var task = new MeshineryTask<String, Context, Context>()
        .inputSource(mockInputSource)
        .read(INPUT_KEY, executor)
        .stopIf(x -> x.getId().equals("1"))
        .process(processorSpy);

    //Act ------------------------------------------------------------------------------------
    new RoundRobinScheduler<>(true, List.of(task)).start();

    //Assert ---------------------------------------------------------------------------------
    executor.awaitTermination(3, TimeUnit.SECONDS);
    Mockito.verify(processorSpy, Mockito.never()).processAsync(any(), any());
  }

  @Value
  @Builder
  private static class TestContext implements Context {
    String id;
  }
}
