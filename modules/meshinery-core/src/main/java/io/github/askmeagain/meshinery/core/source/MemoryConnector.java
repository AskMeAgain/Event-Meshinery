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
  private final ConcurrentHashMap<K, ConcurrentHashMap<String, C>> map = new ConcurrentHashMap<>();

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
      var remove = map.remove(key);
      return new ArrayList<>(remove.values());
    }
    return Collections.emptyList();
  }

  @Override
  public synchronized void writeOutput(K key, C output, TaskData unused) {
    if (map.containsKey(key)) {
      map.get(key).put(output.getId(), output);
    } else {
      var innerMap = new ConcurrentHashMap<String, C>();
      innerMap.put(output.getId(), output);
      map.put(key, innerMap);
    }
  }

  @Override
  public synchronized Optional<C> getContext(K key, String id) {
    var mapOfState = map.getOrDefault(key, new ConcurrentHashMap<>());

    return Optional.ofNullable(mapOfState.get(id));
  }

  @Override
  public C commit(C context) {
    return context;
  }
}
