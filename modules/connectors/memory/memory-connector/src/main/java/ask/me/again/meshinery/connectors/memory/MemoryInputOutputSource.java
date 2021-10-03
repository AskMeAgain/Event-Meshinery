package ask.me.again.meshinery.connectors.memory;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.OutputSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MemoryInputOutputSource<K, C extends Context> implements InputSource<K, C>, OutputSource<K, C> {

  private final ConcurrentHashMap<K, List<C>> map = new ConcurrentHashMap<>();

  @Override
  public List<C> getInputs(K key) {
    return map.replace(key, Collections.synchronizedList(new ArrayList<>()));
  }

  @Override
  public void writeOutput(K key, C output) {
    map.computeIfPresent(key, (k, cs) -> {
      cs.add(output);
      return cs;
    });
    map.computeIfAbsent(key, k -> {
      var list = Collections.synchronizedList(new ArrayList<C>());
      list.add(output);
      return list;
    });
  }
}
