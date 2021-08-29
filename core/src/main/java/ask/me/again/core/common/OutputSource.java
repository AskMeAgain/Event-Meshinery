package ask.me.again.core.common;

import java.util.List;

public interface OutputSource<K, C extends Context> {

  void writeOutput(K key, C output);

}
