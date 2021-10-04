package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@RequiredArgsConstructor
public class StopProcessor<I> implements MeshineryProcessor<I, I> {

  private final Function<I, Boolean> stopIf;

  @Override
  public CompletableFuture<I> processAsync(I context, Executor executor) {

    if (stopIf.apply(context)) {
      return null;
    }

    return CompletableFuture.completedFuture(context);
  }
}
