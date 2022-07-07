package io.github.askmeagain.meshinery.core.other;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
   * the output type of a processor is the input type of the next processor. Essentials this utility method collapses
   * any list of processors
   *
   * @param processorList the list of processors which will run sequentially
   * @param context       the start context which will be passed to the first processor
   * @param executor      the executor which will be used in all processors
   * @param mdc           the log mdc which will be used in each processor
   * @param taskData      the taskdata to be used in each processor
   * @param <I>           input context type of the first processor
   * @param <O>           output context type of the last processor
   * @return returns a completable future which will return O
   */
  public static <I extends DataContext, O extends DataContext> CompletableFuture<O> combineProcessors(
      List<MeshineryProcessor<DataContext, DataContext>> processorList,
      I context,
      Executor executor,
      Map<String, String> mdc,
      TaskData taskData
  ) {
    CompletableFuture<DataContext> temp = CompletableFuture.completedFuture(context);

    for (MeshineryProcessor<DataContext, DataContext> newProcessor : processorList) {
      temp = temp.thenCompose(x -> {
        MDC.setContextMap(mdc);
        TaskData.setTaskData(taskData);
        return newProcessor.processAsync(x, executor);
      });
    }

    return (CompletableFuture<O>) temp;
  }

  /**
   * Applies all decorators to a processor
   *
   * @param nextProcessor      the processor to be decorated
   * @param processorDecorator list of decorators
   * @param <I>                input type of the context
   * @param <O>                output type of the context
   * @return a new processor which will be decorated
   */
  public static <I extends DataContext, O extends DataContext> MeshineryProcessor<I, O> applyDecorators(
      MeshineryProcessor<I, O> nextProcessor,
      List<ProcessorDecorator<I, O>> processorDecorator
  ) {
    var innerProcessor = nextProcessor;

    for (var decorator : processorDecorator) {
      innerProcessor = decorator.wrap(innerProcessor);
    }

    return innerProcessor;
  }

  public static MeshineryConnector<?, ? extends DataContext> applyDecorator(
      MeshineryConnector<?, ? extends DataContext> connector,
      List<InputSourceDecoratorFactory> connectorDecoratorFactories
  ) {
    var innerConnector = connector;

    for (var decorator : connectorDecoratorFactories) {
      innerConnector = decorator.decorate(innerConnector);
    }

    return innerConnector;
  }

  /**
   * Combines all input keys to a single string
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

  public static List<? extends MeshineryTask<?, ? extends DataContext>> decorateMeshineryTasks(
      List<MeshineryTask<?, ? extends DataContext>> tasks,
      List<InputSourceDecoratorFactory> connectorDecoratorFactories
  ) {
    return tasks.stream()
        .map(task -> {
          var decoratedInput = MeshineryUtils.applyDecorator(task.getInputConnector(), connectorDecoratorFactories);

          return task.withNewInputConnector(decoratedInput);
        }).toList();
  }
}
