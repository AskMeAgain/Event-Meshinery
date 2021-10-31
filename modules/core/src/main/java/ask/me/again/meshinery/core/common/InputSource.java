package ask.me.again.meshinery.core.common;

import java.util.List;

@FunctionalInterface
@SuppressWarnings("checkstyle:MissingJavadocType")
public interface InputSource<K, I extends Context> {

  List<I> getInputs(K key);

}
