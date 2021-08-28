package ask.me.again.core;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class PassthroughProcessor<C extends Context> implements ReactiveProcessor<C> {

  private final String name;

  @Override
  public CompletableFuture<C> processAsync(C context) {
    System.out.println("Writing into Kafka Topic: " + name);
    return CompletableFuture.completedFuture(context);
  }
}
