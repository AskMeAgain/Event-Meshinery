package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.OutputSource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

@SuppressWarnings("checkstyle:MissingJavadocType")
public record OutputProcessor<K, C extends Context>(K key, Predicate<C> writeIf, OutputSource<K, C> outputSource)
    implements MeshineryProcessor<C, C> {

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {
    if (writeIf.test(context)) {
      outputSource.writeOutput(key, context);
    }

    return CompletableFuture.completedFuture(context);
  }
}
