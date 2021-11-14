package ask.me.again.meshinery.core.source;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.OutputSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class MemoryConnector<K, C extends Context> implements InputSource<K, C>, OutputSource<K, C> {

  @Getter
  private final String name;
  private final ConcurrentHashMap<K, Queue<C>> map = new ConcurrentHashMap<>();
  private final int batchSize = 100;

  @Override
  public List<C> getInputs(K key) {
    var list = new ArrayList<C>();

    if (map.containsKey(key)) {
      for (int i = 0; i < batchSize; i++) {
        var item = map.get(key).poll();
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
      map.get(key).add(output);
    } else {
      Queue<C> queue = new ConcurrentLinkedQueue<>();
      queue.add(output);
      map.put(key, queue);
    }
  }
}
