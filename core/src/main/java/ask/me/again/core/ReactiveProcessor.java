package ask.me.again.core;

import java.util.concurrent.CompletableFuture;

public interface ReactiveProcessor<C extends Context> {

  CompletableFuture<C> processAsync(C context);
}
