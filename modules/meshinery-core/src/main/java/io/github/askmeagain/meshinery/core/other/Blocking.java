package io.github.askmeagain.meshinery.core.other;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Blocking {

  private static final ConcurrentHashMap<String, Semaphore> internalMap = new ConcurrentHashMap<>();

  @SneakyThrows
  public static <O> O byKey(String key, Supplier<O> action) {
    var semaphore = internalMap.computeIfAbsent(key, k -> new Semaphore(1));
    try {
      semaphore.acquire();
      return action.get();
    } finally {
      internalMap.remove(key);
      semaphore.release();
    }
  }
}
