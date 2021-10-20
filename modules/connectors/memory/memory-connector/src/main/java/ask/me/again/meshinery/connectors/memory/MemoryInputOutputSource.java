package ask.me.again.meshinery.connectors.memory;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.OutputSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryInputOutputSource<K, C extends Context> implements InputSource<K, C>, OutputSource<K, C> {

  private final ConcurrentHashMap<K, Queue<C>> map = new ConcurrentHashMap<>();

  @Override
  public List<C> getInputs(K key) {
    return Collections.emptyList();
    //return map.replace(key, ));
  }

  @Override
  public void writeOutput(K key, C output) {

//    if(map.containsKey(key)){
//      map.get(key).
//    }
//
//    map.put(key, (k, cs) -> {
//      cs.add(output);
//      return cs;
//    });
//    map.computeIfAbsent(key, k -> {
//      var list = Collections.synchronizedList(new ArrayList<C>());
//      list.add(output);
//      return list;
//    });
  }
}
