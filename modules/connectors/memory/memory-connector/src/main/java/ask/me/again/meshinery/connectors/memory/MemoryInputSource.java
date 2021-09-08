package ask.me.again.meshinery.connectors.memory;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;

import java.util.HashMap;
import java.util.List;

public class MemoryInputSource<K, C extends Context> implements InputSource<K, C> {

  private final HashMap<K, C> map = new HashMap<>();

  @Override
  public List<C> getInputs(K key) {
    return List.of(map.remove(key));
  }
}
