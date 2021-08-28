package ask.me.again.core;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class TaskRun<C extends Context> {

  @Setter
  CompletableFuture<C> future;
  Queue<ReactiveProcessor<C>> queue;

  public TaskRun(CompletableFuture<C> future, Queue<ReactiveProcessor<C>> queue) {
    this.future = future;
    this.queue = queue;
  }
}
