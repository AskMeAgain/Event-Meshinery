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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

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
  private final List<K> outputKeys;
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

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  MeshineryTask(
      long backoffTimeMilli,
      List<K> inputKeys,
      List<K> outputKeys,
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
    this.outputKeys = outputKeys;
    this.taskName = taskName;
    this.taskData = taskData;

    this.inputConnectorInternal = inputConnector;
    this.outputConnector = outputConnector;
    this.handleException = handleException;
    this.processorListInternal = processorList;
    this.listInputSourceDecoratorFactories = listInputSourceDecoratorFactories;
    this.listProcessorDecorators = listProcessorDecorators;
  }

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

  public static <K, C extends MeshineryDataContext> MeshineryTaskFactory<K, C> builder() {
    return new MeshineryTaskFactory<>();
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

  public MeshineryTaskFactory<K, C> toBuilder() {
    return new MeshineryTaskFactory<K, C>(
        processorListInternal,
        taskName,
        inputConnectorInternal,
        outputConnector,
        inputKeys,
        outputKeys,
        taskData,
        handleException,
        listProcessorDecorators,
        listInputSourceDecoratorFactories,
        backoffTimeMilli
    );
  }
}
