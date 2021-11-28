package ask.me.again.meshinery.core.source;

import ask.me.again.meshinery.core.common.AccessingInputSource;
import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.OutputSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MemoryConnector<K, C extends Context> implements AccessingInputSource<K, C>, OutputSource<K, C> {

  @Getter
  private String name = "default";
  private final ConcurrentHashMap<K, ConcurrentNavigableMap<String, C>> map = new ConcurrentHashMap<>();
  private final int batchSize = 100;

  public MemoryConnector(String name) {
    this.name = name;
  }

  @Override
  public List<C> getInputs(K key) {
    var list = new ArrayList<C>();

    if (map.containsKey(key)) {
      for (int i = 0; i < batchSize; i++) {
        var stringCConcurrentNavigableMap = map.get(key);
        var stringCEntry = stringCConcurrentNavigableMap.firstEntry();
        stringCConcurrentNavigableMap.remove(stringCEntry.getKey());
        var item = stringCEntry.getValue();
        if (item == null) {
          break;
        }
        list.add(item);
      }
    }

    return list.size() == 0 ? Collections.emptyList() : list;
  }

  @Override
  public void writeOutput(K key, C output) {

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
    var stringCConcurrentNavigableMap = map.get(key);
    if (stringCConcurrentNavigableMap.containsKey(id)) {
      return Optional.of(stringCConcurrentNavigableMap.get(id));
    }
    return Optional.empty();
  }
}
