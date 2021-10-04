package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.OutputSource;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@RequiredArgsConstructor
public class OutputProcessor<K, I> implements MeshineryProcessor<I, I> {

  private final K key;
  private final Function<I, Boolean> writeIf;
  private final OutputSource<K, I> outputSource;

  @Override
  public CompletableFuture<I> processAsync(I context, Executor executor) {
    if (writeIf.apply(context)) {
      outputSource.writeOutput(key, context);
    }

    return CompletableFuture.completedFuture(context);
  }
}
