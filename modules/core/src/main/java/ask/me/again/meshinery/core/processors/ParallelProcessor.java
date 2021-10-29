package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParallelProcessor<Output extends Context> implements MeshineryProcessor<Output, Output> {

  List<MeshineryProcessor<Output, Output>> processorList;
  Function<List<Output>, Output> combine;

  public static <Output extends Context> ParallelProcessor<Output> builder(){
    return new ParallelProcessor<>();
  }

  private ParallelProcessor(){
    processorList = new ArrayList<>();
  }

  public ParallelProcessor<Output> parallel(MeshineryProcessor<Output, Output> processor){
    processorList.add(processor);
    return this;
  }

  public ParallelProcessor<Output> combine(Function<List<Output>, Output> function){
    combine = function;
    return this;
  }

  @Override
  public CompletableFuture<Output> processAsync(Output context, Executor executor) {

    var futures = processorList.stream()
        .map(x -> x.processAsync(context, executor))
        .collect(Collectors.toList());

    return allOf(futures).thenApply(combine);
  }

  private <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
    var array = futuresList.toArray(new CompletableFuture[futuresList.size()]);
    var allFuturesResult = CompletableFuture.allOf(array);

    return allFuturesResult.thenApply(v -> futuresList.stream().
        map(CompletableFuture::join).
        collect(Collectors.<T>toList())
    );
  }
}
