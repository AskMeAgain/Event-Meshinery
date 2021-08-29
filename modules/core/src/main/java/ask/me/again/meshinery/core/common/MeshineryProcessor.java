package ask.me.again.meshinery.core.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface MeshineryProcessor<C extends Context> {

  CompletableFuture<C> processAsync(C context, Executor executor);

}
