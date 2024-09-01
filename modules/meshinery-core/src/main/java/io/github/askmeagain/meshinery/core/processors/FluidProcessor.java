package io.github.askmeagain.meshinery.core.processors;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.MDC;

/**
 * Processor which takes a list of different Processors and returns a single processor which combines the processors.
 * These processors will run in order and they dont necessary need to have the same context type. For example you can
 * process from ContexType A -> B -> C -> A
 *
 * @param <I> InputType
 * @param <O> OutputType
 */
public class FluidProcessor<I extends MeshineryDataContext, O extends MeshineryDataContext>
    implements MeshineryProcessor<I, O> {

  List<MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>> processorList;

  private FluidProcessor() {
    processorList = new ArrayList<>();
  }

  private FluidProcessor(List<MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>> newProcessorList) {
    processorList = newProcessorList;
  }

  /**
   * Initiates a builder for the ComposableProcessor.
   *
   * @param <I> Input Type
   * @return returns itself for builder pattern
   */
  public static <I extends MeshineryDataContext> FluidProcessor<I, I> builder() {
    return new FluidProcessor<>();
  }

  /**
   * Adds a new MeshineryProcessor to the ComposableProcessor.
   *
   * @param newProcessor Processor
   * @param <N>          New return type
   * @return returns itself for builder pattern.
   */
  public <N extends MeshineryDataContext> FluidProcessor<I, N> process(MeshineryProcessor<O, N> newProcessor) {
    processorList.add((MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>) newProcessor);
    return new FluidProcessor<>(processorList);
  }

  @Override
  public O process(I context) {
    var result = MeshineryUtils.combineProcessors(
        processorList,
        context,
        MDC.getCopyOfContextMap(),
        getTaskData()
    ).get();

    return (O) result;
  }
}
