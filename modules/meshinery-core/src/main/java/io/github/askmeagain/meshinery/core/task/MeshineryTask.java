package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.processors.CommitProcessor;
import io.github.askmeagain.meshinery.core.scheduler.ConnectorKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_IGNORE_NO_KEYS_WARNING;

/**
 * A Meshinery task consists of an input source, a list of processors and multiple output sources.
 */
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MeshineryTask<K, C extends MeshineryDataContext> {

  @Getter
  private final long backoffTimeMilli;
  @Getter
  private final List<K> inputKeys;
  @Getter
  private final String taskName;
  @Getter
  private TaskData taskData;
  @Getter
  private final MeshineryOutputSource<K, C> outputConnector;
  @Getter
  private final BiFunction<C, Throwable, C> handleException;
  @Getter(lazy = true)
  private final List<MeshineryProcessor<C, C>> processorList = getProcessorListDecorated();
  @Getter(lazy = true)
  private final MeshineryInputSource<K, C> inputConnector = getInputConnectorDecorated();

  @With(AccessLevel.PRIVATE)
  private final List<InputSourceDecoratorFactory<K, C>> listInputSourceDecoratorFactories;
  @With(AccessLevel.PRIVATE)
  private final List<ProcessorDecorator<C>> listProcessorDecorators;

  private final MeshineryInputSource<K, C> inputConnectorInternal;
  private final List<MeshineryProcessor<C, C>> processorListInternal;

  private Instant nextExecution = Instant.now();

  public MeshineryInputSource<K, C> getInputConnectorDecorated() {
    return MeshineryUtils.applyDecorator(
        inputConnectorInternal,
        listInputSourceDecoratorFactories
    );
  }

  public List<MeshineryProcessor<C, C>> getProcessorListDecorated() {
    var finalList = new ArrayList<>(processorListInternal);
    finalList.add(new CommitProcessor<C>(this::getInputConnector));
    return finalList.stream()
        .map(processor -> MeshineryUtils.applyDecorators(processor, listProcessorDecorators))
        .toList();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask(
      long backoffTimeMilli,
      List<K> inputKeys,
      String taskName,
      TaskData taskData,
      MeshineryInputSource<K, C> inputConnector,
      MeshineryOutputSource<K, C> outputConnector,
      BiFunction<C, Throwable, C> handleException,
      List<MeshineryProcessor<C, C>> processorList,
      List<InputSourceDecoratorFactory<K, C>> listInputSourceDecoratorFactories,
      List<ProcessorDecorator<C>> listProcessorDecorators
  ) {
    if (inputConnector != null) {
      taskData = inputConnector.addToTaskData(taskData);
    }

    this.backoffTimeMilli = backoffTimeMilli;
    this.inputKeys = inputKeys;
    this.taskName = taskName;
    this.taskData = taskData;

    this.inputConnectorInternal = inputConnector;
    this.outputConnector = outputConnector;
    this.handleException = handleException;
    this.processorListInternal = processorList;
    this.listInputSourceDecoratorFactories = listInputSourceDecoratorFactories;
    this.listProcessorDecorators = listProcessorDecorators;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public ConnectorKey getConnectorKey() {
    return ConnectorKey.builder()
        .connector(getInputConnectorDecorated())
        .key(inputKeys)
        .build();
  }

  public MeshineryTask<K, C> withTaskData(TaskData taskData) {
    this.taskData = taskData;
    return this;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public void verifyTask() {
    Objects.requireNonNull(inputConnectorInternal, "Input source not specified");

    if (inputKeys.isEmpty() && !taskData.has(TASK_IGNORE_NO_KEYS_WARNING)) {
      throw new RuntimeException("Input Keys not defined for task %s. ".formatted(taskName)
          + "If this is intended add %s property to task to ignore this".formatted(TASK_IGNORE_NO_KEYS_WARNING));
    }
  }

  /**
   * Pulls the next batch of data from the input source. Keeps the backoff period in mind, which in this case returns
   * empty list and doesnt poll the source
   *
   * @return returns TaskRuns
   */
  public List<TaskRun> getNewTaskRuns() {
    var now = Instant.now();

    if (!now.isAfter(nextExecution)) {
      return Collections.emptyList();
    }

    try {
      TaskData.setTaskData(taskData);
      nextExecution = now.plusMillis(backoffTimeMilli);
      return getInputConnector()
          .getInputs(inputKeys)
          .stream()
          .map(input -> {
            var processorList1 = getProcessorList();
            var queue = new LinkedList<MeshineryProcessor>(processorList1);
            return TaskRun.builder()
                .taskName(getTaskName())
                .taskData(taskData)
                .context(input)
                .handleError((BiFunction<MeshineryDataContext, Throwable, MeshineryDataContext>) handleException)
                .queue(queue)
                .build();
          })
          .toList();
    } finally {
      TaskData.clearTaskData();
    }
  }

  public MeshineryTask<K, C> addInputSourceDecorators(List<InputSourceDecoratorFactory<K, C>> decoratedInput) {
    return this.withListInputSourceDecoratorFactories(decoratedInput);
  }

  public MeshineryTask<K, C> addProcessorDecorators(List<ProcessorDecorator<C>> decoratedProcessors) {
    return this.withListProcessorDecorators(decoratedProcessors);
  }
}
