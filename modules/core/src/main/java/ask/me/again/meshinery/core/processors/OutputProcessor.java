package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.OutputSource;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@RequiredArgsConstructor
public class OutputProcessor<K, C extends Context> implements MeshineryProcessor<C, C> {

  private final Function<C, Boolean> writeIf;
  private final K key;
  private final OutputSource<K, C> outputSource;

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {
    System.out.println("Writing into Kafka Topic: " + key);

    if (writeIf.apply(context)) {
      outputSource.writeOutput(key, context);
    }

    return CompletableFuture.completedFuture(context);
  }
}
