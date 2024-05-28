package io.github.askmeagain.meshinery.core.processors;

///**
// * Processor which will process the provided MeshineryProcessors and runs them in parallel. The executor of this
// * processor will run all the child processors.
// *
// * @param <C> ContextType
// */
//public class ParallelProcessor<C extends MeshineryDataContext> implements MeshineryProcessor<C, C> {
//
//  List<MeshineryProcessor<C, C>> processorList;
//  Function<List<C>, C> combine;
//  Executor executor;
//
//  private ParallelProcessor(
//      List<MeshineryProcessor<C, C>> processorList,
//      Function<List<C>, C> function,
//      Executor executor
//  ) {
//    this.executor = executor;
//    this.processorList = processorList;
//    this.combine = function;
//  }
//
//  public static <C extends MeshineryDataContext> ParallelProcessor.Builder<C> builder() {
//    return new ParallelProcessor.Builder<>();
//  }
//
//  @Override
//  public C processAsync(C context) {
//
//    var futures = processorList.stream()
//        .map(x -> CompletableFuture.supplyAsync(() -> x.processAsync(context), executor))
//        .toList();
//
//    return allOf(futures).thenApply(combine);
//  }
//
//  private <T extends MeshineryDataContext> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
//    var array = futuresList.toArray(new CompletableFuture[futuresList.size()]);
//    var allFuturesResult = CompletableFuture.allOf(array);
//
//    return allFuturesResult.thenApply(result -> futuresList.stream().map(CompletableFuture::join).toList());
//  }
//
//  /**
//   * Builder class of @see ParallelProcessor.
//   *
//   * @param <O> ContextType
//   */
//  public static class Builder<O extends MeshineryDataContext> {
//
//    List<MeshineryProcessor<O, O>> processorList;
//
//    public Builder() {
//      processorList = new ArrayList<>();
//    }
//
//    public ParallelProcessor.Builder<O> parallel(MeshineryProcessor<O, O> processor) {
//      processorList.add(processor);
//      return this;
//    }
//
//    public ParallelProcessor<O> combine(Function<List<O>, O> function) {
//      return new ParallelProcessor<>(processorList, function);
//    }
//  }
//}
