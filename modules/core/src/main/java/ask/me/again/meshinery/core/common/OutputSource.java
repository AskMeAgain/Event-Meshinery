package ask.me.again.meshinery.core.common;

@FunctionalInterface
@SuppressWarnings("checkstyle:MissingJavadocType")
public interface OutputSource<K, C extends Context> {

  void writeOutput(K key, C output);

}
