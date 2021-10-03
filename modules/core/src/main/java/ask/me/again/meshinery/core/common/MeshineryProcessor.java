package ask.me.again.meshinery.core.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface MeshineryProcessor<IC extends Context, OC extends Context> {

  CompletableFuture<OC> processAsync(IC context, Executor executor);

}
