package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MemoryConnector<K, C extends MeshineryDataContext> implements AccessingInputSource<K, C>,
    MeshinerySourceConnector<K, C> {

  @Getter
  private String name = "default-memory-connector";
  private final ConcurrentHashMap<K, List<C>> map = new ConcurrentHashMap<>();
  private final int batchSize = 100;

  public MemoryConnector(String name) {
    this.name = name;
  }

  @Override
  public List<C> getInputs(List<K> keys) {
    return keys.stream()
        .map(this::getInputs)
        .flatMap(Collection::stream)
        .toList();
  }

  @SneakyThrows
  private synchronized List<C> getInputs(K key) {
    if (map.containsKey(key)) {
      return map.remove(key);
    }
    return Collections.emptyList();
  }

  @Override
  public synchronized void writeOutput(K key, C output, TaskData unused) {
    if (map.containsKey(key)) {
      map.get(key).add(output);
    } else {
      var innerMap = new ArrayList<C>();
      innerMap.add(output);
      map.put(key, innerMap);
    }
  }

  @Override
  public Optional<C> getContext(K key, String id) {
    var list = map.getOrDefault(key, Collections.emptyList());

    //TODO make this more efficient
    return list.stream()
        .filter(x -> x.getId().equals(id))
        .findFirst();
  }
}
