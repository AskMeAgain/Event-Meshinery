package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.OutputSource;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@RequiredArgsConstructor
public class DynamicOutputProcessor<K, C extends Context> implements MeshineryProcessor<C> {

  private final Function<C, Boolean> writeIf;
  private final Function<C, K> keyMethod;
  private final OutputSource<K, C> outputSource;

  @Override
  public CompletableFuture<C> processAsync(C context, Executor executor) {

    if (writeIf.apply(context)) {
      outputSource.writeOutput(keyMethod.apply(context), context);
    }

    return CompletableFuture.completedFuture(context);
  }
}
