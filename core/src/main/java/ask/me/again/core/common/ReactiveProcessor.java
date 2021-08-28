package ask.me.again.core.common;

import ask.me.again.core.common.Context;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface ReactiveProcessor<C extends Context> {

  CompletableFuture<C> processAsync(C context, Executor executor);

}
