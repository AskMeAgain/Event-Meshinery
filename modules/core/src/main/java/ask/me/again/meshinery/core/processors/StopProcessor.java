package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class StopProcessor<I extends Context> implements MeshineryProcessor<I, I> {

  private final Function<I, Boolean> stopIf;

  @Override
  public CompletableFuture<I> processAsync(I context, Executor executor) {

    if (stopIf.apply(context)) {
      return CompletableFuture.completedFuture(null);
    }

    return CompletableFuture.completedFuture(context);
  }
}
