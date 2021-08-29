package ask.me.again.core.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class TaskRun<C extends Context> {

  @Setter
  @Getter
  CompletableFuture<C> future;

  @Getter
  @Setter
  ExecutorService executorService;

  @Getter
  Queue<MeshineryProcessor<C>> queue;

  public TaskRun(CompletableFuture<C> future, Queue<MeshineryProcessor<C>> queue, ExecutorService executorService) {
    this.future = future;
    this.queue = queue;
    this.executorService = executorService;
  }
}
