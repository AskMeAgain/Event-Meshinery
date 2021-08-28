package ask.me.again.core;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class OperationEngine<C extends Context> implements Runnable {

  private final List<ReactiveProcessor<C>> operations;

  public static <C extends Context> void construct(String name, List<ReactiveProcessor<C>> operations) {

    new Thread(new OperationEngine<C>(operations), name).start();

  }

  @Override
  @SneakyThrows
  public void run() {

    System.out.println("Thread started");

    for (int i = 0; i < 3; i++) {
      Thread.sleep(1000);
      System.out.println("new iteration!");
      var context = (C) TestContext.builder()
        .testvalue1(10)
        .build();

      for (var processor : operations) {
        var future = processor.processAsync(context);

        if(future.isDone()){

        }
      }
    }
  }

  @RequiredArgsConstructor
  public static class TaskRun {
      private final int index;
      private final Object result;
  }
}
