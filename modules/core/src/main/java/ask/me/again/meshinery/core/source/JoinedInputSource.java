package ask.me.again.meshinery.core.source;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JoinedInputSource<K, C extends Context> implements InputSource<K, C> {

  private final InputSource<K, C> leftInputSource;
  private final InputSource<K, C> rightInputSource;
  private final K rightKey;
  private final BiFunction<C, C, C> combine;

  private final Map<K, Map<String, C>> leftMap = new HashMap<>();
  private final Map<K, Map<String, C>> rightMap = new HashMap<>();

  @Override
  public List<C> getInputs(K key) {

    setup(key, rightKey);

    var leftMap = this.leftMap.get(key);
    var rightMap = this.rightMap.get(rightKey);

    var right = rightInputSource.getInputs(key);
    var left = leftInputSource.getInputs(rightKey);

    var results = new ArrayList<C>();

    //write items straight to map
    right.forEach(context -> rightMap.put(context.getId(), context));
    left.forEach(context -> leftMap.put(context.getId(), context));

    //now find duplicates
    var leftKeys = leftMap.keySet();
    var rightKeys = rightMap.keySet();

    leftKeys.retainAll(rightKeys);

    var duplicated = new HashSet<>(leftKeys);

    for (var id : duplicated) {
      var leftContext = leftMap.remove(id);
      var rightContext = rightMap.remove(id);
      results.add(combine.apply(leftContext, rightContext));
    }

    return results;
  }

  private void setup(K leftKey, K rightKey) {
    if (!leftMap.containsKey(leftKey)) {
      leftMap.put(leftKey, new HashMap<>());
    }
    if (!rightMap.containsKey(rightKey)) {
      rightMap.put(rightKey, new HashMap<>());
    }
  }
}
