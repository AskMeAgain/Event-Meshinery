package ask.me.again.meshinery.core.common;

@FunctionalInterface
public interface OutputSource<K, C extends Context> {

  void writeOutput(K key, C output);

}
