package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.OutputSource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@SuppressWarnings("checkstyle:MissingJavadocType")
public record OutputProcessor<K, I extends Context>(K key, Function<I, Boolean> writeIf,
    OutputSource<K, I> outputSource) implements MeshineryProcessor<I, I> {

  @Override
  public CompletableFuture<I> processAsync(I context, Executor executor) {
    if (writeIf.apply(context)) {
      outputSource.writeOutput(key, context);
    }

    return CompletableFuture.completedFuture(context);
  }
}
