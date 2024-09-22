package io.github.askmeagain.meshinery.aop.config;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Holds internal futures for return values of aop methods
 */
@Service
public class AopFutureHolderService implements AutoCloseable {

  private final ConcurrentHashMap<String, CompletableFuture<MeshineryDataContext>> futures = new ConcurrentHashMap<>();

  /**
   * removes the future if it exists and returns it
   *
   * @param key the key
   * @return the future
   */
  public CompletableFuture<MeshineryDataContext> getFuture(String key) {
    return futures.remove(key);
  }

  /**
   * Creates a future and writes it into an internal map
   * @param key key of the map
   * @return returns the created future
   */
  public CompletableFuture<MeshineryDataContext> createFuture(String key) {
    var future = new CompletableFuture<MeshineryDataContext>();
    futures.put(key, future);
    return future;
  }

  @Override
  public void close() {
    futures.forEach((key, value) -> value.completeExceptionally(new Throwable("Shutdown of application")));
  }
}
