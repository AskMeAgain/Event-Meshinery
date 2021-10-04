package ask.me.again.meshinery.core.common;

import java.util.List;

public interface InputSource<K, I> {

  List<I> getInputs(K key);

}
