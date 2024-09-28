package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.InputSourceDecorator;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.processors.DynamicOutputProcessor;
import io.github.askmeagain.meshinery.core.processors.SignalingProcessor;
import io.github.askmeagain.meshinery.core.processors.StopProcessor;
import io.github.askmeagain.meshinery.core.source.JoinedInnerInputSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.core.other.MeshineryUtils.joinEventKeys;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
@Builder(toBuilder = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class MeshineryTaskFactory<K, C extends MeshineryDataContext> {

  private long backoffTime;
  private List<K> inputKeys;
  private List<K> outputKeys = new ArrayList<>();
  private String taskName = "default-task-" + hashCode();
  private MeshineryInputSource<K, C> inputConnector;
  private MeshineryOutputSource<K, C> outputConnector;
  private BiFunction<C, Throwable, C> handleException = (context, exc) -> {
    if (exc != null) {
      throw new RuntimeException(exc);
    }
    return context;
  };

  private TaskData taskData = new TaskData().with(TaskDataProperties.TASK_NAME, taskName);
  private List<MeshineryProcessor<C, C>> processorList = new ArrayList<>();

  @Singular private List<ProcessorDecorator<C>> processorDecorators = new ArrayList<>();
  @Singular private List<InputSourceDecorator<K, C>> inputSourceDecorators = new ArrayList<>();

  MeshineryTaskFactory(
      MeshineryProcessor<C, C> newProcessor,
      List<MeshineryProcessor<C, C>> oldProcessorList,
      String taskName,
      MeshineryInputSource<K, C> inputConnector,
      MeshineryOutputSource<K, C> outputConnector,
      List<K> inputKeys,
      List<K> outputKeys,
      TaskData taskData,
      BiFunction<C, Throwable, C> handleException,
      List<ProcessorDecorator<C>> processorDecoratorList,
      List<InputSourceDecorator<K, C>> inputSourceDecoratorFactoryList,
      long backoffTime
  ) {
    var newProcessorList = new ArrayList<>(oldProcessorList);
    newProcessorList.add(newProcessor);

    this.taskName = taskName;
    this.backoffTime = backoffTime;
    this.processorDecorators = processorDecoratorList;
    this.inputSourceDecorators = inputSourceDecoratorFactoryList;
    this.processorList = newProcessorList;
    this.inputConnector = inputConnector;
    this.outputConnector = outputConnector;
    this.inputKeys = inputKeys;
    this.outputKeys = outputKeys;
    this.taskData = taskData;
    this.handleException = handleException;
  }

  MeshineryTaskFactory(
      List<MeshineryProcessor<C, C>> oldProcessorList,
      String name,
      MeshineryInputSource<K, C> inputConnector,
      MeshineryOutputSource<K, C> outputConnector,
      List<K> inputKeys,
      List<K> outputKeys,
      TaskData taskData,
      BiFunction<C, Throwable, C> handleException,
      List<ProcessorDecorator<C>> processorDecoratorList,
      List<InputSourceDecorator<K, C>> inputSourceDecoratorFactoryList,
      long backoffTime
  ) {
    taskName = name;
    this.backoffTime = backoffTime;
    this.processorDecorators = processorDecoratorList;
    this.inputSourceDecorators = inputSourceDecoratorFactoryList;
    this.processorList = oldProcessorList;
    this.inputConnector = inputConnector;
    this.outputConnector = outputConnector;
    this.inputKeys = inputKeys;
    this.outputKeys = outputKeys;
    this.taskData = taskData;
    this.handleException = handleException;
  }

  /**
   * Specifies the default output source of a task. This can be overridden by switching the context
   * or by providing another Outputsource to a write() call
   *
   * @param outputSource The source
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> outputSource(MeshineryOutputSource<K, C> outputSource) {
    return toBuilder()
        .outputConnector(outputSource)
        .taskData(taskData.with(TaskDataProperties.GRAPH_OUTPUT_SOURCE, outputSource.getName()))
        .build();
  }

  /**
   * The Inputsource of this MeshineryTask.
   *
   * @param inputSource The source
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> inputSource(MeshineryInputSource<K, C> inputSource) {
    return toBuilder()
        .inputConnector(inputSource)
        .taskData(this.taskData.with(TaskDataProperties.GRAPH_INPUT_SOURCE, inputSource.getName()))
        .build();
  }

  /**
   * This method will assign the inputConnector and the outputConnector
   * to the task. calls inputSource() and outputSource under the hood.
   *
   * @param connector connector to be assigned
   * @return new immutable meshinery task factory
   */
  public MeshineryTaskFactory<K, C> connector(MeshinerySourceConnector<K, C> connector) {
    return toBuilder()
        .inputConnector(connector)
        .outputConnector(connector)
        .taskData(this.taskData.with(TaskDataProperties.GRAPH_INPUT_SOURCE, connector.getName()))
        .build();
  }

  /**
   * Reads from the inputsource with the provided key. Uses the executorService to query the inputData.
   *
   * @param inputKeys The Key to be used in the Inputsource
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> read(K... inputKeys) {
    return toBuilder()
        .inputKeys(List.of(inputKeys))
        .taskData(taskData.with(TaskDataProperties.GRAPH_INPUT_KEY, joinEventKeys(inputKeys)))
        .build();
  }

  /**
   * Attaches task data to this task which can be used by processors and input/output sources
   *
   * @param key   key of the data
   * @param value value fo the data
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> putData(String key, String value) {
    return toBuilder()
        .taskData(taskData.with(key, value))
        .build();
  }

  /**
   * Attaches task data to this task which can be used by processors and input/output sources. Takes in a list in the
   * format of key=value entries
   *
   * @param kvs strings in the format of key=value
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> putData(List<String> kvs) {
    for (var kv : kvs) {
      var arr = kv.split("=");
      taskData = taskData.with(arr[0], arr[1]);
    }

    return toBuilder()
        .taskData(taskData)
        .build();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTaskFactory<K, C> putData(String key) {
    return putData(key, "1");
  }

  public MeshineryTaskFactory<K, C> readNewInput(K key, AccessingInputSource<K, C> newInputSource) {
    return addNewProcessor(new SignalingProcessor<>(newInputSource, key, (context, signal) -> signal));
  }

  public MeshineryTaskFactory<K, C> readNewInput(
      K key,
      AccessingInputSource<K, C> newInputSource,
      BiFunction<C, C, C> join
  ) {
    return addNewProcessor(new SignalingProcessor<>(newInputSource, key, join));
  }

  /**
   * Adds another inputsource which gets joined on. As join key the context Id will be used.
   *
   * @param rightInputSource  the right side of the join sources
   * @param rightKey          the key of the right source
   * @param timeToLiveSeconds window size of the join
   * @param combine           the combine method used to be applied to the join results
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> joinOn(
      MeshinerySourceConnector<K, C> rightInputSource,
      K rightKey,
      int timeToLiveSeconds,
      BiFunction<C, C, C> combine
  ) {
    var name = "%s->%s__%s->%s".formatted(inputConnector.getName(), inputKeys, rightInputSource.getName(), rightKey);
    var joinedSource = new JoinedInnerInputSource<>(
        name,
        inputConnector,
        rightInputSource,
        rightKey,
        combine,
        timeToLiveSeconds
    );
    return toBuilder()
        .inputConnector(joinedSource)
        .taskData(taskData
            .with(TaskDataProperties.GRAPH_INPUT_SOURCE, rightInputSource.getName())
            .with(TaskDataProperties.GRAPH_INPUT_KEY, rightKey.toString()))
        .build();
  }

  /**
   * Method used to set a name for a MeshineryTask. This name will be used for the drawer methods and general logging
   *
   * @param name the name
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> taskName(String name) {
    return toBuilder()
        .taskName(name)
        .taskData(taskData.replace(TaskDataProperties.TASK_NAME, name))
        .build();
  }

  /**
   * Adds a processor to a MeshineryTask, which stops the processing of a single event when the provided Predicate
   * returns true. Equivalent to Java Streams .filter() method
   *
   * @param stopIf Predicate to use
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> stopIf(Predicate<C> stopIf) {
    return addNewProcessor(new StopProcessor<>(stopIf));
  }

  /**
   * Adds a new processor the the list. Will be run in order
   *
   * @param processor will be added
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> process(MeshineryProcessor<C, C> processor) {
    var decorated = MeshineryUtils.applyDecorators(processor, processorDecorators);
    return addNewProcessor(decorated);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTaskFactory<K, C> registerProcessorDecorator(ProcessorDecorator<C> decorator) {
    return toBuilder()
        .processorDecorator(decorator)
        .build();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTaskFactory<K, C> registerProcessorDecorator(Collection<ProcessorDecorator<C>> decorator) {
    return toBuilder()
        .processorDecorators(decorator)
        .build();
  }

  /**
   * registers an input source decorator which decorates the input source for this task only
   *
   * @param decorator the decorator
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> registerInputSourceDecorator(InputSourceDecorator<K, C> decorator) {
    return toBuilder()
        .inputSourceDecorator(decorator)
        .build();
  }

  /**
   * registers multiple input source decorators which decorates the input source for this task only
   * @param decorator the decorator
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> registerInputSourceDecorator(
      Collection<? extends InputSourceDecorator<K, C>> decorator
  ) {
    return toBuilder()
        .inputSourceDecorators(decorator)
        .build();
  }

  /**
   * Writes an event to an OutputSource.
   *
   * @param key          the key to be used in the OutputSource
   * @param outputSource the OutputSource which will be used
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> write(K key, MeshinerySourceConnector<K, C> outputSource) {
    return write(key, x -> true, outputSource);
  }

  /**
   * Writes an event for each provided key to an OutputSource.
   *
   * @param keys keys to be used
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> write(K... keys) {
    var temp = this;
    for (K key : keys) {
      temp = temp.write(key, x -> true, outputConnector);
    }
    return temp;
  }

  /**
   * Writes an event if a predicate returns true. Takes an Outputsource which overrides the defaultOutputSource
   *
   * @param key          Key to be used
   * @param writeIf      predicate to be used
   * @param outputSource The outputsource to be used.
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> write(K key, Predicate<C> writeIf, MeshineryOutputSource<K, C> outputSource) {
    var newTaskData = taskData.with(TaskDataProperties.GRAPH_OUTPUT_SOURCE, outputSource.getName())
        .with(TaskDataProperties.GRAPH_OUTPUT_KEY, key.toString());
    outputKeys.add(key);

    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, c -> key, outputSource))
        .toBuilder()
        .outputKeys(outputKeys)
        .taskData(newTaskData)
        .build();
  }

  /**
   * Writes an event if a predicate returns true.
   *
   * @param key     Key to be used
   * @param writeIf predicate to be used
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> write(K key, Predicate<C> writeIf) {
    outputKeys.add(key);
    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, c -> key, outputConnector))
        .toBuilder()
        .outputKeys(outputKeys)
        .taskData(taskData.with(TaskDataProperties.GRAPH_OUTPUT_KEY, key.toString()))
        .build();
  }

  /**
   * Writes an event if a predicate returns true and uses a dynamic KeyFunction. Uses the defaultOutputSource.
   *
   * @param keyFunction Keyfunction to be used
   * @param writeIf     predicate to be used
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> write(Function<C, K> keyFunction, Predicate<C> writeIf) {
    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, keyFunction, outputConnector));
  }

  /**
   * Writes an event if a predicate returns true and uses a dynamic KeyFunction. Uses the defaultOutputSource.
   *
   * @param keyFunction Keyfunction to be used
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> write(Function<C, K> keyFunction) {
    return addNewProcessor(new DynamicOutputProcessor<>(x -> true, keyFunction, outputConnector));
  }

  /**
   * Writes an event if a predicate returns true and uses a dynamic KeyFunction. Uses the provided OutputSource
   *
   * @param keyFunction     Keyfunction to be used
   * @param writeIf         predicate to be used
   * @param newOutputSource Outputsource to be used
   * @return returns itself for builder pattern
   */
  public final MeshineryTaskFactory<K, C> write(
      Function<C, K> keyFunction,
      Predicate<C> writeIf,
      MeshinerySourceConnector<K, C> newOutputSource
  ) {
    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, keyFunction, newOutputSource))
        .toBuilder()
        .taskData(taskData.with(TaskDataProperties.GRAPH_OUTPUT_SOURCE, newOutputSource.getName()))
        .build();
  }

  /**
   * Registers a method handler, which is getting executed when an exception happens INSIDE the completable future.
   * Allows to return a different value instead. Passes the handleError function to the .handle() method of
   * completable future
   *
   * @param handleError The method which will be passed to the .handle() method of completable future
   * @return returns itself for builder pattern
   */
  public final MeshineryTaskFactory<K, C> exceptionHandler(BiFunction<C, Throwable, C> handleError) {
    return toBuilder()
        .handleException(handleError)
        .build();
  }

  /**
   * Adds an interval between input source polling.
   *
   * @param milliSeconds the time in milliseconds
   * @return returns itself for builder pattern
   */
  public final MeshineryTaskFactory<K, C> backoffTime(long milliSeconds) {
    return toBuilder()
        .backoffTime(milliSeconds)
        .build();
  }

  /**
   * Add task data to your task. Some connectors and processors use this for configuration
   *
   * @param taskData
   * @return
   */
  public final MeshineryTaskFactory<K, C> taskData(TaskData taskData) {
    return toBuilder()
        .taskData(taskData)
        .build();
  }

  private MeshineryTaskFactory<K, C> addNewProcessor(MeshineryProcessor<C, C> newProcessor) {
    var meshineryTaskFactory = new MeshineryTaskFactory<K, C>(
        newProcessor,
        processorList,
        taskName,
        inputConnector,
        outputConnector,
        inputKeys,
        outputKeys,
        taskData,
        handleException,
        processorDecorators,
        inputSourceDecorators,
        backoffTime
    );
    return meshineryTaskFactory.toBuilder()
        .taskData(newProcessor.addToTaskData(
            taskData.with(TaskDataProperties.GRAPH_PROCESSOR, newProcessor.getClass().getName())))
        .build();
  }

  /**
   * Build the task.
   *
   * @return returns immutable MeshineryTask
   */
  public MeshineryTask<K, C> build() {
    return new MeshineryTask<>(
        backoffTime,
        inputKeys,
        outputKeys,
        taskName,
        taskData,
        inputConnector,
        outputConnector,
        handleException,
        processorList,
        inputSourceDecorators,
        processorDecorators
    );
  }
}
