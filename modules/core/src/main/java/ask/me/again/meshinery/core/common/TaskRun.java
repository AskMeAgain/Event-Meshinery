package ask.me.again.meshinery.core.common;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@Builder
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class TaskRun {

  String id;
  String taskName;

  @With
  CompletableFuture<Context> future;

  MdcInjectingExecutorService executorService;

  Queue<MeshineryProcessor<Context, Context>> queue;

  Function<Throwable, Context> handleError;
}
