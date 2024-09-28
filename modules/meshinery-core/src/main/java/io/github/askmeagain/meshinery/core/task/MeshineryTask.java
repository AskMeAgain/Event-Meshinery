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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * A Meshinery task consists of an input source, a list of processors and multiple output sources.
 */
@Slf4j
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
  private final List<MeshineryProcessor<C, C>> processorList = decorateProcessors();
  @Getter(lazy = true)
  private final MeshineryInputSource<K, C> inputConnector = decorateInputSource();

  private final List<InputSourceDecoratorFactory<K, C>> listInputSourceDecoratorFactories;
  private final List<ProcessorDecorator<C>> listProcessorDecorators;

  private final MeshineryInputSource<K, C> internalInputSource;
  private final List<MeshineryProcessor<C, C>> internalProcessorList;

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

    this.internalInputSource = inputConnector;
    this.outputConnector = outputConnector;
    this.handleException = handleException;
    this.internalProcessorList = processorList;
    this.listInputSourceDecoratorFactories = listInputSourceDecoratorFactories;
    this.listProcessorDecorators = listProcessorDecorators;
  }

  public static <K, C extends MeshineryDataContext> MeshineryTaskFactory<K, C> builder() {
    return new MeshineryTaskFactory<>();
  }

  public MeshineryInputSource<K, C> decorateInputSource() {
    return MeshineryUtils.applyDecorator(internalInputSource, listInputSourceDecoratorFactories);
  }

  public List<MeshineryProcessor<C, C>> decorateProcessors() {
    var finalList = new ArrayList<>(internalProcessorList);
    finalList.add(new CommitProcessor<C>(this::getInputConnector));
    return finalList.stream()
        .map(processor -> MeshineryUtils.applyDecorators(processor, listProcessorDecorators))
        .toList();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public ConnectorKey getConnectorKey() {
    return ConnectorKey.builder()
        .connector(decorateInputSource())
        .key(inputKeys)
        .build();
  }

  public MeshineryTask<K, C> withTaskData(TaskData taskData) {
    this.taskData = taskData;
    return this;
  }

  public MeshineryTaskFactory<K, C> toBuilder() {
    return new MeshineryTaskFactory<>(
        internalProcessorList,
        taskName,
        internalInputSource,
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
