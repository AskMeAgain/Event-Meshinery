package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LambdaProcessor<Input extends Context, Output extends Context>
    implements MeshineryProcessor<Input, Output> {

  private final Function<Input, Output> map;

  @Override
  public CompletableFuture<Output> processAsync(Input context, Executor executor) {
    return CompletableFuture.completedFuture(map.apply(context));
  }
}
