package io.github.askmeagain.meshinery.core.other;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.OutputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.processors.CommitProcessor;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

@SuppressWarnings("checkstyle:MissingJavadocType")
@UtilityClass
public class MeshineryUtils {

  /**
   * This utility method takes a list of processors and chains them together sequentially via completable future
   * compose method.
   * Note that the input and output type of each processor can be different, but needs to be correct as long as
   * the output type of a processor is the input type of the next processor. Essentially this utility method
   * collapses
   * any list of processors
   *
   * @param processorList the list of processors which will run sequentially
   * @param context       the start context which will be passed to the first processor
   * @param mdc           the log mdc which will be used in each processor
   * @param taskData      the taskdata to be used in each processor
   * @return returns a completable future which will return O
   */
  public static Supplier<MeshineryDataContext> combineProcessors(
      List<MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>> processorList,
      MeshineryDataContext context,
      Map<String, String> mdc,
      TaskData taskData
  ) {
    return () -> {
      var temp = context;
      MDC.setContextMap(mdc);
      TaskData.setTaskData(taskData);
      for (MeshineryProcessor<MeshineryDataContext, MeshineryDataContext> newProcessor : processorList) {
        temp = newProcessor.processAsync(temp);
      }
      return temp;
    };
  }

  /**
   * Applies all decorators to a processor.
   *
   * @param nextProcessor      the processor to be decorated
   * @param processorDecorator list of decorators
   * @param <I>                input type of the context
   * @param <O>                output type of the context
   * @return a new processor which will be decorated
   */
  public static <I extends MeshineryDataContext, O extends MeshineryDataContext> MeshineryProcessor<I, O> applyDecorators(
      MeshineryProcessor<I, O> nextProcessor,
      List<ProcessorDecorator<I, O>> processorDecorator
  ) {
    var innerProcessor = nextProcessor;

    for (var decorator : processorDecorator) {
      innerProcessor = decorator.wrap(innerProcessor);
    }

    return innerProcessor;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static MeshineryInputSource<?, ? extends MeshineryDataContext> applyDecorator(
      MeshineryInputSource<?, ? extends MeshineryDataContext> connector,
      List<InputSourceDecoratorFactory> connectorDecoratorFactories
  ) {
    var innerConnector = connector;

    for (var decorator : connectorDecoratorFactories) {
      innerConnector = decorator.decorate(innerConnector);
    }

    return innerConnector;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static <K, V extends MeshineryDataContext> MeshineryOutputSource<?, ? extends MeshineryDataContext> applyDecorator(
      MeshineryOutputSource<K, V> connector,
      List<OutputSourceDecoratorFactory> connectorDecoratorFactories
  ) {
    var innerConnector = connector;

    for (var decorator : connectorDecoratorFactories) {
      innerConnector = (MeshineryOutputSource<K, V>) decorator.decorate(innerConnector);
    }

    return innerConnector;
  }

  /**
   * Combines all input keys to a single string.
   *
   * @param inputKeys list of input keys
   * @param <K>       type of the input key
   * @return a string
   */
  @SafeVarargs
  public static <K> String joinEventKeys(K... inputKeys) {
    return Arrays.stream(inputKeys)
        .map(Object::toString)
        .collect(Collectors.joining("-"));
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static List<? extends MeshineryTask<?, ? extends MeshineryDataContext>> decorateMeshineryTasks(
      List<MeshineryTask<?, ? extends MeshineryDataContext>> tasks,
      List<InputSourceDecoratorFactory> connectorDecoratorFactories
  ) {
    //TODO fix this
    return tasks.stream()
        .map(task -> {
          var decoratedInput = (MeshineryInputSource<?, MeshineryDataContext>) MeshineryUtils.applyDecorator(
              task.getInputConnector(),
              connectorDecoratorFactories
          );

          var meshineryTask = task.withNewInputConnector(decoratedInput);
          meshineryTask.getProcessorList().add(
              new CommitProcessor<>(() -> decoratedInput)
          );
          return meshineryTask;
        }).toList();
  }
}
