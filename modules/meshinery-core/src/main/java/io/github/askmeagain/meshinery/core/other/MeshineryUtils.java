package io.github.askmeagain.meshinery.core.other;

import io.github.askmeagain.meshinery.core.common.InputSourceDecorator;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.OutputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_IGNORE_NO_KEYS_WARNING;

@SuppressWarnings("checkstyle:MissingJavadocType")
@UtilityClass
public class MeshineryUtils {

  /**
   * This utility method takes a list of processors and chains them together sequentially via completable future
   * compose method.
   * Note that the input and output type of each processor can be different, but needs to be correct as long as
   * the output type of a processor is the input type of the next processor. Essentially this utility method
   * collapses any list of processors
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
        temp = newProcessor.process(temp);
      }
      return temp;
    };
  }

  public static <C extends MeshineryDataContext> MeshineryProcessor<C, C> applyDecorators(
      MeshineryProcessor<C, C> nextProcessor,
      List<ProcessorDecorator<C>> processorDecorator
  ) {
    var innerProcessor = nextProcessor;

    for (var decorator : processorDecorator) {
      innerProcessor = decorator.wrap(innerProcessor);
    }

    return innerProcessor;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static <K, C extends MeshineryDataContext> MeshineryInputSource<K, C> applyDecorator(
      MeshineryInputSource<K, C> connector,
      List<InputSourceDecorator<K, C>> connectorDecoratorFactories
  ) {
    var innerConnector = connector;

    for (var decorator : connectorDecoratorFactories) {
      innerConnector = decorator.decorate(innerConnector);
    }

    return innerConnector;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static <K, V extends MeshineryDataContext> MeshineryOutputSource<?, ? extends MeshineryDataContext>
  applyDecorator(
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
  public static void verifyTask(MeshineryTask task) {
    Objects.requireNonNull(task.getInputConnector(), "Input source not specified");

    if (task.getInputKeys().isEmpty() && !task.getTaskData().has(TASK_IGNORE_NO_KEYS_WARNING)) {
      throw new RuntimeException("Input Keys not defined for task %s. ".formatted(task.getTaskName())
          + "If this is intended add %s property to task to ignore this".formatted(TASK_IGNORE_NO_KEYS_WARNING));
    }
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public static List<MeshineryTask<?, ?>> injectProperties(
      List<MeshineryTask<?, ?>> tasks,
      MeshineryCoreProperties coreProperties
  ) {
    var newList = new ArrayList<MeshineryTask<?, ?>>();

    //TODO this can eb probably changed to just inserting the KV directly without converting to taskdata
    for (var task : tasks) {
      if (coreProperties.getTaskProperties().containsKey(task.getTaskName())) {
        var newTaskData = Optional.ofNullable(task.getTaskData()).orElse(new TaskData());
        for (var kv : coreProperties.getTaskProperties().get(task.getTaskName()).entrySet()) {
          newTaskData = newTaskData.with(kv.getKey(), kv.getValue());
        }
        newList.add(task.withTaskData(newTaskData));
      } else {
        newList.add(task);
      }
    }

    return newList;
  }
}
