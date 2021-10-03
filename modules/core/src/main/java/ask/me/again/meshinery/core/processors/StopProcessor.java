package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@RequiredArgsConstructor
public class StopProcessor<C extends Context> implements MeshineryProcessor<C, C> {

  private final Function<C, Boolean> stopIf;

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {

    if (stopIf.apply(context)) {
      return null;
    }

    return CompletableFuture.completedFuture(context);
  }
}
