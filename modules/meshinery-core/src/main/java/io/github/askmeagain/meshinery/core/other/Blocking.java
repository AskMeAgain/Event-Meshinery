package io.github.askmeagain.meshinery.core.other;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Slf4j
@UtilityClass
public class Blocking {

  private static final ConcurrentHashMap<String, Semaphore> internalMap = new ConcurrentHashMap<>();

  public static <O> O byKey(String key, Supplier<O> action) {
    return byKey(new String[]{key}, action);
  }

  public static <O> O byKey(List<String> keys, Supplier<O> action) {
    return byKey(keys.toArray(String[]::new), action);
  }

  /**
   * Provide an array of keys and the supplied action will be executed thread safe and BLOCKING for all keys.
   * if thread 1 enters keys: A,B,C and thread 2 uses keys C,D,E then this will block for thread 2 until thread 1 is
   * done since the key C is blocking for all threads. This is used to make sure that accessing input sources are not
   * interfering with the normal execution of input source, in case the input source is not thread safe.
   *
   * @param keys   the keys which will be used to determine if the supplier is blocked or not
   * @param action the action to be used
   * @param <O>    the type of the supplier return value
   * @return returns the value from the supplier
   */
  @SneakyThrows
  public static <O> O byKey(String[] keys, Supplier<O> action) {
    var semaphores = new Semaphore[keys.length];
    try {
      for (int i = 0; i < keys.length; i++) {
        semaphores[i] = internalMap.computeIfAbsent(keys[i], k -> new Semaphore(1));
        semaphores[i].acquire();
      }
      return action.get();
    } finally {
      for (int i = 0; i < semaphores.length; i++) {
        internalMap.remove(keys[i]);
        semaphores[i].release();
      }
    }
  }
}
