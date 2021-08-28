package ask.me.again.core.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;

public class TaskRun<C extends Context> {

  @Setter
  @Getter
  CompletableFuture<C> future;

  @Getter
  Queue<ReactiveProcessor<C>> queue;

  public TaskRun(CompletableFuture<C> future, Queue<ReactiveProcessor<C>> queue) {
    this.future = future;
    this.queue = queue;
  }
}
