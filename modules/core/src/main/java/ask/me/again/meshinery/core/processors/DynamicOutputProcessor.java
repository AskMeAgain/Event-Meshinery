package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.OutputSource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Processor which writes to an OutputSource with a dynamic key, calculated for each Context
 *
 * @param <K> KeyType
 * @param <C> ContextType
 */
public record DynamicOutputProcessor<K, C extends Context>(Predicate<C> writeIf, Function<C, K> keyMethod,
    OutputSource<K, C> outputSource) implements MeshineryProcessor<C, C> {

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {

    if (writeIf.test(context)) {
      outputSource.writeOutput(keyMethod.apply(context), context);
    }

    return CompletableFuture.completedFuture(context);
  }
}
