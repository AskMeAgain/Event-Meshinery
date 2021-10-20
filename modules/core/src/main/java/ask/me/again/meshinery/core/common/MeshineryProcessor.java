package ask.me.again.meshinery.core.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@FunctionalInterface
public interface MeshineryProcessor<I extends Context, O extends Context> {

  CompletableFuture<O> processAsync(I context, Executor executor);

}
