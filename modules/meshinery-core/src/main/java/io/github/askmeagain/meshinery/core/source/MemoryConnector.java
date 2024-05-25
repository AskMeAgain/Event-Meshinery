package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.other.Blocking;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MemoryConnector<K, C extends MeshineryDataContext> implements AccessingInputSource<K, C>,
    MeshinerySourceConnector<K, C> {

  @Getter
  private String name = "default-memory-connector";
  private final ConcurrentHashMap<K, ConcurrentNavigableMap<String, C>> map = new ConcurrentHashMap<>();
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
  private List<C> getInputs(K key) {
    return Blocking.<List<C>>byKey(
        key.toString(),
        () -> {
          var list = new ArrayList<C>();
          if (map.containsKey(key)) {
            for (int i = 0; i < batchSize; i++) {
              var stringConcurrentNavigableMap = map.get(key);
              if (stringConcurrentNavigableMap.isEmpty()) {
                break;
              }
              var stringEntry = stringConcurrentNavigableMap.firstEntry();
              stringConcurrentNavigableMap.remove(stringEntry.getKey());
              var item = stringEntry.getValue();
              if (item == null) {
                break;
              }
              list.add(item);
            }
          }
          return list;
        }
    );
  }

  @Override
  public void writeOutput(K key, C output, TaskData unused) {
    if (map.containsKey(key)) {
      map.get(key).put(output.getId(), output);
    } else {
      var innerMap = new ConcurrentSkipListMap<String, C>();
      innerMap.put(output.getId(), output);
      map.put(key, innerMap);
    }
  }

  @Override
  public Optional<C> getContext(K key, String id) {
    return Blocking.byKey(key + "_" + id, () -> {
      var stringConcurrentNavigableMap = map.get(key);
      if (stringConcurrentNavigableMap.containsKey(id)) {
        return Optional.of(stringConcurrentNavigableMap.get(id));
      }
      return Optional.empty();
    });
  }
}
