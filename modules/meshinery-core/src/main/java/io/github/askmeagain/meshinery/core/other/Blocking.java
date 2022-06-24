package io.github.askmeagain.meshinery.core.other;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;
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

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
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
