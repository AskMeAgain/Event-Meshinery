package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.PassiveExpiringMap;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class JoinedInnerInputSource<K, C extends DataContext> implements MeshineryConnector<K, C> {

  @Getter
  private final String name;
  private final MeshineryConnector<K, C> leftInputSource;
  private final MeshineryConnector<K, C> rightInputSource;
  private final K rightKey;
  private final BiFunction<C, C, C> combine;
  private final int timeToLiveSeconds;

  private final Map<K, Map<String, C>> leftJoinResultsMap = new HashMap<>();
  private final Map<K, Map<String, C>> rightJoinResultsMap = new HashMap<>();

  @SneakyThrows
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
      var leftItem = leftMap.get(id);
      var rightItem = rightMap.get(id);

      if (leftItem == null) {
        leftMap.remove(id);
      }
      if (rightItem == null) {
        rightMap.remove(id);
      }

      if (leftItem != null && rightItem != null) {
        var leftContext = leftMap.remove(id);
        var rightContext = rightMap.remove(id);

        results.add(combine.apply(leftContext, rightContext));
      }
    }

    return results;
  }

  private void setup(K leftKey, K rightKey) {
    if (!leftJoinResultsMap.containsKey(leftKey)) {
      leftJoinResultsMap.put(leftKey, createCache());
    }
    if (!rightJoinResultsMap.containsKey(rightKey)) {
      rightJoinResultsMap.put(rightKey, createCache());
    }
  }

  private Map<String, C> createCache() {
    var expirationPolicy = new PassiveExpiringMap.ConstantTimeToLiveExpirationPolicy<String, C>(
        timeToLiveSeconds, TimeUnit.SECONDS
    );

    return new PassiveExpiringMap<>(expirationPolicy);
  }

  @Override
  public void writeOutput(K key, C output) {
    throw new UnsupportedOperationException();
  }
}
