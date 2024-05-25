package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import java.time.Instant;
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

  @Builder.Default
  Instant now = Instant.now();
  String id;
  String taskName;

  @With
  CompletableFuture<MeshineryDataContext> future;

  DataInjectingExecutorService executorService;

  Queue<MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>> queue;

  Function<Throwable, MeshineryDataContext> handleError;
  TaskData taskData;
}
