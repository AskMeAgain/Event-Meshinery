package io.github.askmeagain.meshinery.aop.config;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AopFutureHolderService implements AutoCloseable {

  private ConcurrentHashMap<String, CompletableFuture<MeshineryDataContext>> futures = new ConcurrentHashMap<>();

  public CompletableFuture<MeshineryDataContext> getFuture(String key) {
    return futures.remove(key);
  }

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
