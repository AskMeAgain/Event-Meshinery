package ask.me.again.core.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface ReactiveProcessor<C extends Context> {

  CompletableFuture<C> processAsync(C context, Executor executor);

}
