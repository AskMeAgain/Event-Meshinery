package ask.me.again.meshinery.core.processors;

import ask.me.again.meshinery.core.common.DataContext;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.other.MeshineryUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.MDC;

/**
 * Processor which takes a list of different Processors and returns a single processor which combines the processors.
 * These processors will run in order and they dont necessary need to have the same context type. For example you can
 * process from ContexType A -> B -> C -> A
 *
 * @param <I> InputType
 * @param <O> OutputType
 */
public class FluidProcessor<I extends DataContext, O extends DataContext> implements MeshineryProcessor<I, O> {

  List<MeshineryProcessor<DataContext, DataContext>> processorList;

  private FluidProcessor() {
    processorList = new ArrayList<>();
  }

  private FluidProcessor(List<MeshineryProcessor<DataContext, DataContext>> newProcessorList) {
    processorList = newProcessorList;
  }

  /**
   * Initiates a builder for the ComposableProcessor.
   *
   * @param <I> Input Type
   * @return returns itself for builder pattern
   */
  public static <I extends DataContext> FluidProcessor<I, I> builder() {
    return new FluidProcessor<>();
  }

  /**
   * Adds a new MeshineryProcessor to the ComposableProcessor.
   *
   * @param newProcessor Processor
   * @param <N>          New return type
   * @return returns itself for builder pattern.
   */
  public <N extends DataContext> FluidProcessor<I, N> process(MeshineryProcessor<O, N> newProcessor) {
    processorList.add((MeshineryProcessor<DataContext, DataContext>) newProcessor);
    return new FluidProcessor<>(processorList);
  }

  @Override
  public CompletableFuture<O> processAsync(I context, Executor executor) {
    return MeshineryUtils.combineProcessors(
        processorList,
        context,
        executor,
        MDC.getCopyOfContextMap(),
        getTaskData()
    );
  }
}
