package ask.me.again.meshinery.core.common;

public interface OutputSource<K, C> {

  void writeOutput(K key, C output);

}
