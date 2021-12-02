package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.task.TaskDataProperties;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_INPUT_KEY;

@Builder
public class SignalingInputSource<K, C extends DataContext> implements InputSource<K, C> {

  @Getter
  private final String name;
  private final InputSource<K, C> signalingInputSource;
  private final InputSource<K, C> innerInputSource;
  private final K innerKey;

  @Override
  public TaskData addToTaskData(TaskData taskData) {
    return taskData.put(GRAPH_INPUT_KEY, innerKey.toString());
  }

  @Override
  public List<C> getInputs(K key) {

    var signal = signalingInputSource.getInputs(key);

    if (signal.isEmpty()) {
      return Collections.emptyList();
    }

    return innerInputSource.getInputs(innerKey);
  }
}
