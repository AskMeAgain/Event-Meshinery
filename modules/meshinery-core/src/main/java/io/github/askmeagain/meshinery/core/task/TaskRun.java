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
public class TaskRun<C extends MeshineryDataContext> {

  @Builder.Default
  Instant now = Instant.now();
  String taskName;

  C context;

  Queue<MeshineryProcessor<C, C>> queue;

  BiFunction<C, Throwable, C> handleError;

  TaskData taskData;
}
