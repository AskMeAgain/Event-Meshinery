package ask.me.again.meshinery.core.source;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class JoinedInputSource<K, C extends Context> implements InputSource<K, C> {

  private final InputSource<K, C> leftInputSource;
  private final InputSource<K, C> rightInputSource;
  private final K rightKey;
  private final BiFunction<C, C, C> combine;

  private final Map<K, Map<String, C>> leftJoinResultsMap = new HashMap<>();
  private final Map<K, Map<String, C>> rightJoinResultsMap = new HashMap<>();

  private final AtomicInteger counter = new AtomicInteger(1);

  @Override
  public List<C> getInputs(K key) {

    setup(key, rightKey);

    var leftMap = this.leftJoinResultsMap.get(key);
    var rightMap = this.rightJoinResultsMap.get(rightKey);

    var left = leftInputSource.getInputs(key);
    var right = rightInputSource.getInputs(rightKey);

    var results = new ArrayList<C>();

    //write items straight to map
    left.forEach(context -> leftMap.put(context.getId(), context));
    right.forEach(context -> rightMap.put(context.getId(), context));

    //now find duplicates
    var leftKeys = new HashSet<>(leftMap.keySet());
    var rightKeys = new HashSet<>(rightMap.keySet());

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
    if (!leftJoinResultsMap.containsKey(leftKey)) {
      leftJoinResultsMap.put(leftKey, new HashMap<>());
    }
    if (!rightJoinResultsMap.containsKey(rightKey)) {
      rightJoinResultsMap.put(rightKey, new HashMap<>());
    }
  }
}
