package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
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
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;

import static io.github.askmeagain.meshinery.core.other.MeshineryUtils.joinEventKeys;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Builder(toBuilder = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MeshineryTaskFactory<K, C extends MeshineryDataContext> {

  private List<K> inputKeys;
  private String taskName = "default-task-" + hashCode();
  private long backoffTime;
  private MeshineryInputSource<K, C> inputConnector;
  private MeshineryOutputSource<K, C> outputConnector;
  private BiFunction<MeshineryDataContext, Throwable, MeshineryDataContext> handleException = (context, exc) -> {
    if (exc != null) {
      throw new RuntimeException(exc);
    }
    return context;
  };

  private TaskData taskData = new TaskData().with(TaskDataProperties.TASK_NAME, taskName);
  private List<MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>> processorList = new ArrayList<>();
  @Singular private List<ProcessorDecorator<C, C>> decorators = new ArrayList<>();

  private <I extends MeshineryDataContext> MeshineryTaskFactory(
      MeshineryProcessor<I, C> newProcessor,
      List<MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>> oldProcessorList,
      String name,
      MeshineryInputSource inputConnector,
      MeshineryOutputSource outputConnector,
      List<K> eventKeys,
      TaskData taskData,
      BiFunction<MeshineryDataContext, Throwable, MeshineryDataContext> handleException,
      long backoffTime
  ) {
    var newProcessorList = new ArrayList<>(oldProcessorList);
    newProcessorList.add((MeshineryProcessor<MeshineryDataContext, MeshineryDataContext>) newProcessor);

    taskName = name;
    this.backoffTime = backoffTime;
    this.processorList = newProcessorList;
    this.inputConnector = inputConnector;
    this.outputConnector = outputConnector;
    this.inputKeys = eventKeys;
    this.taskData = taskData;
    this.handleException = handleException;
  }

  public static <K, C extends MeshineryDataContext> MeshineryTaskFactory<K, C> builder() {
    return new MeshineryTaskFactory<>();
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

  public MeshineryTaskFactory<K, C> connector(MeshinerySourceConnector<K, C> connector) {
    return this.inputSource(connector)
        .outputSource(connector);
  }

  /**
   * Reads from the inputsource with the provided key. Uses the executorService to query the inputData.
   *
   * @param inputKeys       The Key to be used in the Inputsource
   * @return returns itself for builder pattern
   */
  @SafeVarargs
  public final MeshineryTaskFactory<K, C> read(K... inputKeys) {
    return toBuilder()
        .inputKeys(List.of(inputKeys))
        .taskData(taskData.with(TaskDataProperties.GRAPH_INPUT_KEY, joinEventKeys(inputKeys)))
        .build();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTaskFactory<K, C> putData(String key, String value) {
    return toBuilder()
        .taskData(taskData.with(key, value))
        .build();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
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
      K key, AccessingInputSource<K, C> newInputSource, BiFunction<C, C, C> join
  ) {
    return addNewProcessor(new SignalingProcessor<>(newInputSource, key, join));
  }

  /**
   * Adds another inputsource which gets joined on. As join key the context Id will be used.
   *
   * @param rightInputSource the right side of the join sources
   * @param rightKey         the key of the right source
   * @param combine          the combine method used to be applied to the join results
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> joinOn(
      MeshinerySourceConnector<K, C> rightInputSource,
      K rightKey,
      int timeToLiveSeconds,
      BiFunction<C, C, C> combine
  ) {
    var name = "%s->%s__%s->%s".formatted(inputConnector.getName(), inputKeys, rightInputSource.getName(), rightKey);

    return toBuilder()
        .inputConnector(
            new JoinedInnerInputSource<>(name, inputConnector, rightInputSource, rightKey, combine, timeToLiveSeconds))
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
   * Context switch in a task. Needs a mapping method and a new defaultOutputsource to write to.
   *
   * @param newOutputSource new Outputsource to be used further down in the processor list
   * @param map             mapping function from one Context to another
   * @return returns itself for builder pattern
   */
  public <N extends MeshineryDataContext> MeshineryTaskFactory<K, N> contextSwitch(
      MeshinerySourceConnector<K, N> newOutputSource,
      Function<C, N> map
  ) {
    return contextSwitch(newOutputSource, map, Collections.emptyList());
  }

  /**
   * Context switch in a task. Needs a mapping method and a new defaultOutputsource to write to.
   *
   * @param newOutputSource new Outputsource to be used further down in the processor list
   * @param map             mapping function from one Context to another
   * @param <N>             Type of the new Context
   * @return returns itself for builder pattern
   */
  public <N extends MeshineryDataContext> MeshineryTaskFactory<K, N> contextSwitch(
      MeshinerySourceConnector<K, N> newOutputSource,
      Function<C, N> map,
      List<ProcessorDecorator<N, N>> decorators
  ) {
    MeshineryProcessor<C, N> newProcessor = map::apply;

    var newTaskData = inputConnector.addToTaskData(
        taskData.with(TaskDataProperties.GRAPH_OUTPUT_SOURCE, newOutputSource.getName())
    );

    return addNewProcessor(newProcessor)
        .toBuilder()
        .outputConnector(newOutputSource)
        .clearDecorators()
        .decorators(decorators)
        .taskData(newTaskData)
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
    var decorated = MeshineryUtils.applyDecorators(processor, decorators);

    return addNewProcessor(decorated);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTaskFactory<K, C> registerDecorator(ProcessorDecorator<C, C> decorator) {
    return toBuilder()
        .decorator(decorator)
        .build();
  }

  /**
   * Writes an event to an OutputSource.
   *
   * @param key          the key to be used in the OutputSource
   * @param outputSource the OutputSource which will be used
   * @return returns itself for builder pattern
   */
  public final MeshineryTaskFactory<K, C> write(K key, MeshinerySourceConnector<K, C> outputSource) {
    return write(key, x -> true, outputSource);
  }

  /**
   * Writes an event for each provided key to an OutputSource.
   *
   * @param keys keys to be used
   * @return returns itself for builder pattern
   */
  @SafeVarargs
  public final MeshineryTaskFactory<K, C> write(K... keys) {
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
  public final MeshineryTaskFactory<K, C> write(K key, Predicate<C> writeIf, MeshineryOutputSource<K, C> outputSource) {
    var newTaskData = taskData.with(TaskDataProperties.GRAPH_OUTPUT_SOURCE, outputSource.getName())
        .with(TaskDataProperties.GRAPH_OUTPUT_KEY, key.toString());

    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, c -> key, outputSource))
        .toBuilder()
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
  public final MeshineryTaskFactory<K, C> write(K key, Predicate<C> writeIf) {
    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, c -> key, outputConnector))
        .toBuilder()
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
  public final MeshineryTaskFactory<K, C> write(Function<C, K> keyFunction, Predicate<C> writeIf) {
    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, keyFunction, outputConnector));
  }

  /**
   * Writes an event if a predicate returns true and uses a dynamic KeyFunction. Uses the defaultOutputSource.
   *
   * @param keyFunction Keyfunction to be used
   * @return returns itself for builder pattern
   */
  public final MeshineryTaskFactory<K, C> write(Function<C, K> keyFunction) {
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
  public final MeshineryTaskFactory<K, C> exceptionHandler(
      BiFunction<MeshineryDataContext, Throwable, MeshineryDataContext> handleError
  ) {
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

  private <N extends MeshineryDataContext> MeshineryTaskFactory<K, N> addNewProcessor(
      MeshineryProcessor<C, N> newProcessor
  ) {
    return new MeshineryTaskFactory<>(
        newProcessor,
        processorList,
        taskName,
        inputConnector,
        outputConnector,
        inputKeys,
        taskData,
        handleException,
        backoffTime
    ).toBuilder()
        .taskData(newProcessor.addToTaskData(
            taskData.with(TaskDataProperties.GRAPH_PROCESSOR, newProcessor.getClass().getName())))
        .build();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<K, C> build() {
    var finalProcessorList = new ArrayList<>(processorList);
    finalProcessorList.add(context -> inputConnector.commit((C) context));

    return new MeshineryTask<>(
        backoffTime,
        inputKeys,
        taskName,
        taskData,
        inputConnector,
        outputConnector,
        handleException,
        finalProcessorList
    );
  }
}
