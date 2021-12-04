package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.Builder;
import lombok.Getter;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_INPUT_KEY;

@Builder
public class SignalingInputSource<K extends Comparable<K>, C extends DataContext> implements InputSource<K, C> {

  @Builder.Default
  private final boolean lockIn = false;


  private final Set<K> locked = new ConcurrentSkipListSet<>();

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

    if (!lockIn || !locked.contains(key)) {
      var signal = signalingInputSource.getInputs(key);

      if (signal.isEmpty()) {
        return Collections.emptyList();
      }
    }

    var result = innerInputSource.getInputs(innerKey);

    if (lockIn) {
      var isLockedIn = locked.contains(key);
      if (isLockedIn && result.isEmpty()) {
        locked.remove(key);
      } else if(!isLockedIn){
        locked.add(key);
      }
    }

    return result;
  }
}
