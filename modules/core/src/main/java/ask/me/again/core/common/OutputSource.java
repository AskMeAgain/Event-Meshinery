package ask.me.again.core.common;

public interface OutputSource<K, C extends Context> {

  void writeOutput(K key, C output);

}
