package ask.me.again.core.common;

import java.util.List;

public interface InputSource<K, C extends Context> {

  List<C> getInputs(K key);

}
