package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * Processor which takes a lambda and maps from one context to another.
 *
 * @param <I> InputType
 * @param <O> OutputType
 */
public record LambdaProcessor<I extends Context, O extends Context>(Function<I, O> map)
    implements MeshineryProcessor<I, O> {

  @Override
  public CompletableFuture<O> processAsync(I context, Executor executor) {
    return CompletableFuture.completedFuture(map.apply(context));
  }
}
