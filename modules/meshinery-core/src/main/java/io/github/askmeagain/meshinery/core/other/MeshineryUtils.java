package io.github.askmeagain.meshinery.core.other;

import io.github.askmeagain.meshinery.core.common.InputSourceDecorator;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.OutputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.exceptions.DuplicateReadKeyException;
import io.github.askmeagain.meshinery.core.exceptions.DuplicateTaskNameException;
import io.github.askmeagain.meshinery.core.exceptions.TaskNameInvalidException;
import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_IGNORE_DUPLICATE_READ_KEY;
import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_IGNORE_NO_KEYS_WARNING;

@SuppressWarnings("checkstyle:MissingJavadocType")
@UtilityClass
public class MeshineryUtils {

  private static final String VALID_LETTERS = "abcdefghijklmnopqrstuvwxyz_-1234567890";

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

    for (var task : tasks) {
      if (coreProperties.getTaskProperties().containsKey(task.getTaskName())) {
        var builder = task.toBuilder();
        for (var kv : coreProperties.getTaskProperties().get(task.getTaskName()).entrySet()) {
          builder = builder.putData(kv.getKey(), kv.getValue());
        }
        newList.add(builder.build());
      } else {
        newList.add(task);
      }
    }

    return newList;
  }

  public static Set<String> getOutputSources(List<MeshineryTask> tasks) {
    return tasks.stream()
        .map(MeshineryTask::getOutputConnector)
        .map(MeshineryOutputSource::getName)
        .collect(Collectors.toSet());
  }

  public static Set<String> getInputSources(List<MeshineryTask> tasks) {
    return tasks.stream()
        .map(MeshineryTask::getInputConnector)
        .map(MeshineryInputSource::getName)
        .collect(Collectors.toSet());
  }

  public static List<String> getAndVerifyTaskList(List<MeshineryTask> tasks) {
    var result = tasks.stream()
        .map(MeshineryTask::getTaskName)
        .map(MeshineryUtils::verifyTaskName)
        .toList();

    var duplicates = findDuplicates(result);

    if (!duplicates.isEmpty()) {
      throw new DuplicateTaskNameException("Found duplicate task names: [" + String.join(", ", duplicates) + "]");
    }

    return result;
  }

  public static void verifyTasks(List<MeshineryTask> tasks) {
    tasks.forEach(MeshineryUtils::verifyTask);

    var result = tasks.stream()
        .filter(task -> !task.getTaskData().has(TASK_IGNORE_DUPLICATE_READ_KEY))
        .map(MeshineryTask::getInputKeys)
        .flatMap(Collection::stream)
        .map(Object::toString)
        .toList();

    var duplicates = findDuplicates(result);

    if (!duplicates.isEmpty()) {
      throw new DuplicateReadKeyException("Found duplicate Read keys: [" + String.join(", ", duplicates) + "]");
    }
  }

  //https://stackoverflow.com/a/31641116/5563263
  private static <T> Set<T> findDuplicates(Collection<T> collection) {
    Set<T> uniques = new HashSet<>();
    return collection.stream()
        .filter(e -> !uniques.add(e))
        .collect(Collectors.toSet());
  }

  private static String verifyTaskName(String name) {
    for (var letter : name.toCharArray()) {
      if (!VALID_LETTERS.contains(String.valueOf(letter).toLowerCase())) {
        throw new TaskNameInvalidException(
            "Task '%s' contains '%s', but only %s is allowed".formatted(name, letter, VALID_LETTERS));
      }
      if (StringUtils.isBlank(name)) {
        throw new TaskNameInvalidException("Taskname cannot be blank");
      }
    }
    return name;
  }
}
