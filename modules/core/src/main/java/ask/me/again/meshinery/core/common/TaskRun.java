package ask.me.again.meshinery.core.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class TaskRun {

  @Setter
  @Getter
  CompletableFuture<Object> future;

  @Getter
  @Setter
  ExecutorService executorService;

  @Getter
  Queue<MeshineryProcessor<Object, Object>> queue;

  public TaskRun(CompletableFuture<Object> future, Queue<MeshineryProcessor<Object, Object>> queue, ExecutorService executorService) {
    this.future = future;
    this.queue = queue;
    this.executorService = executorService;
  }
}
