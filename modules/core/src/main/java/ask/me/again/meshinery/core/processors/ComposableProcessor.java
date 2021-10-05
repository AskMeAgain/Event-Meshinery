package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.MeshineryProcessor;
import lombok.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Value
@Builder
public class ComposableProcessor<I, O, N> implements MeshineryProcessor<I, N> {

  @Singular
  List<MeshineryProcessor<I, O>> adds;

  @NonNull
  Function<List<O>, N> combine;

  @Override
  public CompletableFuture<N> processAsync(I context, Executor executor) {

    var futures = adds.stream()
        .map(x -> x.processAsync(context, executor))
        .collect(Collectors.toList());

    return allOf(futures).thenApply(combine);
  }

  public <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
    var array = futuresList.toArray(new CompletableFuture[futuresList.size()]);
    var allFuturesResult = CompletableFuture.allOf(array);

    return allFuturesResult.thenApply(v -> futuresList.stream().
        map(CompletableFuture::join).
        collect(Collectors.<T>toList())
    );
  }
}
