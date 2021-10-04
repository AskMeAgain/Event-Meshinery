package ask.me.again.meshinery.core.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface MeshineryProcessor<I, O> {

  CompletableFuture<O> processAsync(I context, Executor executor);

}
