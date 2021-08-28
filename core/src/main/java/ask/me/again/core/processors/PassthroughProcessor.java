package ask.me.again.core.processors;

import ask.me.again.core.common.Context;
import ask.me.again.core.common.ReactiveProcessor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class PassthroughProcessor<C extends Context> implements ReactiveProcessor<C> {

  private final String name;

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {
    System.out.println("Writing into Kafka Topic: " + name);
    return CompletableFuture.completedFuture(context);
  }
}
