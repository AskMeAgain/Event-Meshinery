package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@SuppressWarnings("checkstyle:MissingJavadocType")
public record LambdaProcessor<Input extends Context, Output extends Context>(Function<Input, Output> map)
    implements MeshineryProcessor<Input, Output> {

  @Override
  public CompletableFuture<Output> processAsync(Input context, Executor executor) {
    return CompletableFuture.completedFuture(map.apply(context));
  }
}
