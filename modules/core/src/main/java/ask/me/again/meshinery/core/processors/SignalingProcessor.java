package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.AccessingInputSource;
import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.task.TaskData;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static ask.me.again.meshinery.core.task.TaskDataProperties.GRAPH_INPUT_KEY;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
@RequiredArgsConstructor
public class SignalingProcessor<K, C extends Context> implements MeshineryProcessor<C, C> {

  private final AccessingInputSource<K, C> inputSource;
  private final K key;

  @Override
  public TaskData addToTaskData(TaskData taskData) {
    return taskData.put(GRAPH_INPUT_KEY, key.toString());
  }

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {
    var result = inputSource.getContext(key, context.getId());

    if (result.isEmpty()) {
      return CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.completedFuture(result.get());
  }
}