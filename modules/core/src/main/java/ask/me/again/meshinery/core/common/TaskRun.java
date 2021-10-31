package ask.me.again.meshinery.core.common;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class TaskRun {

  @Setter
  @Getter
  CompletableFuture<Context> future;

  @Getter
  @Setter
  ExecutorService executorService;

  @Getter
  Queue<MeshineryProcessor<Context, Context>> queue;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public TaskRun(
      CompletableFuture<Context> future, Queue<MeshineryProcessor<Context, Context>> queue,
      ExecutorService executorService
  ) {
    this.future = future;
    this.queue = queue;
    this.executorService = executorService;
  }
}
