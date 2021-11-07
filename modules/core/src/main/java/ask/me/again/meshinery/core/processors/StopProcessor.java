package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

/**
 * Processor which takes a predicate. It will return null if the predicate is true, which means the scheduler will
 * stop processing this entry further. Is equivalent to .filter() in Java streams
 *
 * @param <C> Context type
 */
public record StopProcessor<C extends Context>(Predicate<C> stopIf) implements MeshineryProcessor<C, C> {

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {

    if (stopIf.test(context)) {
      return CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.completedFuture(context);
  }
}
