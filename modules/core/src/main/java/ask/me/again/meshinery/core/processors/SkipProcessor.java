package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@RequiredArgsConstructor
public class SkipProcessor<C extends Context> implements MeshineryProcessor<C> {

  private final Function<C, Boolean> skipIf;

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {

    if (skipIf.apply(context)) {
      return null;
    }

    return CompletableFuture.completedFuture(context);
  }
}
