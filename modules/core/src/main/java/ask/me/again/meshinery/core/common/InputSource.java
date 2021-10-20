package ask.me.again.meshinery.core.common;

import java.util.List;

@FunctionalInterface
public interface InputSource<K, I extends Context> {

  List<I> getInputs(K key);

}
