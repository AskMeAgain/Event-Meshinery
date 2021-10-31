package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.OutputSource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DynamicOutputProcessor<K, I extends Context> implements MeshineryProcessor<I, I> {

  private final Function<I, Boolean> writeIf;
  private final Function<I, K> keyMethod;
  private final OutputSource<K, I> outputSource;

  @Override
  public CompletableFuture<I> processAsync(I context, Executor executor) {

    if (writeIf.apply(context)) {
      outputSource.writeOutput(keyMethod.apply(context), context);
    }

    return CompletableFuture.completedFuture(context);
  }
}
