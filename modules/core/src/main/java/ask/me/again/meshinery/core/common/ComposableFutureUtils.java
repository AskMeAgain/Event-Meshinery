package ask.me.again.meshinery.core.common;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.experimental.UtilityClass;

@SuppressWarnings("checkstyle:MissingJavadocType")
@UtilityClass
public class ComposableFutureUtils {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static <I extends Context, O extends Context> CompletableFuture<O> getoCompletableFuture(
      List<MeshineryProcessor<Context, Context>> processorList, I context, Executor executor
  ) {
    CompletableFuture<Context> temp = CompletableFuture.completedFuture(context);

    for (MeshineryProcessor<Context, Context> newProcessor : processorList) {
      temp = temp.thenCompose(x -> newProcessor.processAsync(x, executor));
    }
    
    return (CompletableFuture<O>) temp;
  }
}
