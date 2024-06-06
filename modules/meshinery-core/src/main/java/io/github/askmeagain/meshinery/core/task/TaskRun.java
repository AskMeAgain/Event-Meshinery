package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.time.Instant;
import java.util.Queue;
import java.util.function.BiFunction;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@Builder
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class TaskRun {

  @Builder.Default
  Instant now = Instant.now();
  String taskName;

  MeshineryDataContext context;

  Queue<MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>> queue;

  BiFunction<MeshineryDataContext, Throwable, MeshineryDataContext> handleError;

  TaskData taskData;
}
