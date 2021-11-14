package ask.me.again.meshinery.core.common;

import java.util.List;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface InputSource<K, I extends Context> {

  String getName();
  List<I> getInputs(K key);

}
