package ask.me.again.meshinery.core.task;

import ask.me.again.meshinery.core.common.DataContext;
import ask.me.again.meshinery.core.other.DataInjectingExecutorService;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
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
  CompletableFuture<DataContext> future;

  DataInjectingExecutorService executorService;

  Queue<MeshineryProcessor<DataContext, DataContext>> queue;

  Function<Throwable, DataContext> handleError;
  TaskData taskData;
}
