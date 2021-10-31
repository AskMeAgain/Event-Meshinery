package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.OutputSource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@SuppressWarnings("checkstyle:MissingJavadocType")
public record OutputProcessor<Key, Input extends Context>(Key key, Function<Input, Boolean> writeIf,
    OutputSource<Key, Input> outputSource) implements MeshineryProcessor<Input, Input> {

  @Override
  public CompletableFuture<Input> processAsync(Input context, Executor executor) {
    if (writeIf.apply(context)) {
      outputSource.writeOutput(key, context);
    }

    return CompletableFuture.completedFuture(context);
  }
}
