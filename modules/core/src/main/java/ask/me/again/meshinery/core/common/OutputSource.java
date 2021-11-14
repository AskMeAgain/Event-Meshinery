package ask.me.again.meshinery.core.common;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface OutputSource<K, C extends Context> {

  String getName();

  void writeOutput(K key, C output);

}
