package io.github.askmeagain.meshinery.core.processors;

import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_INPUT_KEY;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
@RequiredArgsConstructor
public class SignalingProcessor<K, C extends DataContext> implements MeshineryProcessor<C, C> {

  private final AccessingInputSource<K, C> inputSource;
  private final K key;
  private final BiFunction<C, C, C> join;

  @Override
  public TaskData addToTaskData(TaskData taskData) {
    return taskData.with(GRAPH_INPUT_KEY, key.toString());
  }

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {
    var result = inputSource.getContext(key, context.getId());

    if (result.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.completedFuture(join.apply(context, result.get()));
  }
}
