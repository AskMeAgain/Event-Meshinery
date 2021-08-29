package ask.me.again.core.processors;

import ask.me.again.core.common.Context;
import ask.me.again.core.common.OutputSource;
import ask.me.again.core.common.ReactiveProcessor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
public class OutputProcessor<K, C extends Context> implements ReactiveProcessor<C> {

  private final K key;
  private final OutputSource<K, C> outputSource;

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {
    System.out.println("Writing into Kafka Topic: " + key);
    outputSource.writeOutput(key, context);

    return CompletableFuture.completedFuture(context);
  }
}
