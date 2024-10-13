package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.InputSourceDecorator;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.exceptions.TaskNotInitializedException;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.processors.CommitProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import lombok.Getter;

/**
 * A Meshinery task consists of an input source, a list of processors and multiple output sources.
 */
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
  private final TaskData taskData;
  @Getter
  private final MeshineryOutputSource<K, C> outputConnector;
  @Getter
  private final BiFunction<C, Throwable, C> handleException;

  private List<MeshineryProcessor<C, C>> processorList;
  private MeshineryInputSource<K, C> inputConnector;
  private boolean initialized = false;

  public List<MeshineryProcessor<C, C>> getProcessorList() {
    if (!initialized) {
      throw new TaskNotInitializedException();
    }
    return processorList;
  }

  public MeshineryInputSource<K, C> getInputConnector() {
    if (!initialized) {
      throw new TaskNotInitializedException();
    }
    return inputConnector;
  }

  public MeshineryTask<K, C> initialize() {
    initialized = true;
    processorList = decorateProcessors();
    inputConnector = decorateInputSource();
    return this;
  }

  private final List<InputSourceDecorator<K, C>> inputSourceDecorators;
  private final List<ProcessorDecorator<C>> processorDecorators;

  private final MeshineryInputSource<K, C> internalInputSource;
  private final List<MeshineryProcessor<C, C>> internalProcessorList;

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
      List<InputSourceDecorator<K, C>> inputSourceDecorators,
      List<ProcessorDecorator<C>> processorDecorators
  ) {
    if (inputConnector != null) {
      taskData = inputConnector.addToTaskData(taskData);
    }

    this.backoffTimeMilli = backoffTimeMilli;
    this.inputKeys = inputKeys;
    this.outputKeys = outputKeys;
    this.taskName = taskName;
    this.taskData = taskData;
    this.outputConnector = outputConnector;
    this.handleException = handleException;
    this.processorDecorators = processorDecorators;
    this.inputSourceDecorators = inputSourceDecorators;

    this.internalInputSource = inputConnector;
    this.internalProcessorList = processorList;
  }

  public static <K, C extends MeshineryDataContext> MeshineryTaskFactory<K, C> builder() {
    return new MeshineryTaskFactory<>();
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
        processorDecorators,
        inputSourceDecorators,
        backoffTimeMilli
    );
  }

  private MeshineryInputSource<K, C> decorateInputSource() {
    return MeshineryUtils.applyDecorator(internalInputSource, Objects.requireNonNull(inputSourceDecorators));
  }

  private List<MeshineryProcessor<C, C>> decorateProcessors() {
    var finalList = new ArrayList<>(Objects.requireNonNull(internalProcessorList));
    finalList.add(new CommitProcessor<>(this::getInputConnector));
    return finalList.stream()
        .map(processor -> MeshineryUtils.applyDecorators(processor, Objects.requireNonNull(processorDecorators)))
        .toList();
  }
}
