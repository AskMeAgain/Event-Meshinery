package io.github.askmeagain.meshinery.core.processors;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Processor which writes to an OutputSource with a dynamic key, calculated for each Context.
 *
 * @param <K> KeyType
 * @param <C> ContextType
 */
public record DynamicOutputProcessor<K, C extends MeshineryDataContext>(
    Predicate<C> writeIf,
    Function<C, K> keyMethod,
    MeshineryOutputSource<K, C> outputSource
) implements MeshineryProcessor<C, C> {

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {
    if (writeIf.test(context)) {
      outputSource.writeOutput(keyMethod.apply(context), context, getTaskData());
    }

    return CompletableFuture.completedFuture(context);
  }
}
