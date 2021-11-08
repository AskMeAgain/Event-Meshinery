package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Processor which takes a list of different Processors and returns a single processor which combines the processors.
 * These processors will run in order and they dont necessary need to have the same context type. For example you can
 * process from ContexType A -> B -> C -> A
 *
 * @param <I> InputType
 * @param <O> OutputType
 */
public class FluidProcessor<I extends Context, O extends Context> implements MeshineryProcessor<I, O> {

  List<MeshineryProcessor<Context, Context>> processorList;

  private FluidProcessor() {
    processorList = new ArrayList<>();
  }

  private FluidProcessor(List<MeshineryProcessor<Context, Context>> newProcessorList) {
    processorList = newProcessorList;
  }

  /**
   * Initiates a builder for the ComposableProcessor.
   *
   * @param <I> Input Type
   * @return returns itself for builder pattern
   */
  public static <I extends Context> FluidProcessor<I, I> builder() {
    return new FluidProcessor<>();
  }

  /**
   * Adds a new MeshineryProcessor to the ComposableProcessor.
   *
   * @param newProcessor Processor
   * @param <N>          New return type
   * @return returns itself for builder pattern.
   */
  public <N extends Context> FluidProcessor<I, N> process(MeshineryProcessor<O, N> newProcessor) {
    processorList.add((MeshineryProcessor<Context, Context>) newProcessor);
    return new FluidProcessor<>(processorList);
  }

  @Override
  public CompletableFuture<O> processAsync(I context, Executor executor) {
    CompletableFuture<Context> temp = CompletableFuture.completedFuture(context);

    for (MeshineryProcessor<Context, Context> newProcessor : processorList) {
      temp = temp.thenCompose(x -> newProcessor.processAsync(x, executor));
    }
    return (CompletableFuture<O>) temp;
  }
}
