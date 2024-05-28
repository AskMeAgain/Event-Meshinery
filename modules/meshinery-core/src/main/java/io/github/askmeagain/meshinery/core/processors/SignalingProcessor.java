package io.github.askmeagain.meshinery.core.processors;

import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_INPUT_KEY;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
@RequiredArgsConstructor
public class SignalingProcessor<K, C extends MeshineryDataContext> implements MeshineryProcessor<C, C> {

  private final AccessingInputSource<K, C> inputSource;
  private final K key;
  private final BiFunction<C, C, C> join;

  @Override
  public TaskData addToTaskData(TaskData taskData) {
    return taskData.with(GRAPH_INPUT_KEY, key.toString());
  }

  @Override
  public C processAsync(C context) {
    return inputSource.getContext(key, context.getId())
        .map(c -> join.apply(context, c))
        .orElse(null);
  }
}
