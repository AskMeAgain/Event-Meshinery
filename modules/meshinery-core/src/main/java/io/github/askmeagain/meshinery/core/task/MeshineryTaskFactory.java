package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.ProcessorDecorator;
import io.github.askmeagain.meshinery.core.other.DataInjectingExecutorService;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.processors.DynamicOutputProcessor;
import io.github.askmeagain.meshinery.core.processors.SignalingProcessor;
import io.github.askmeagain.meshinery.core.processors.StopProcessor;
import io.github.askmeagain.meshinery.core.source.JoinedInnerInputSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Builder(toBuilder = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MeshineryTaskFactory<K, C extends DataContext> {

  private K inputKey;
  private String taskName = "default";
  private long backoffTime;
  private MeshineryConnector<K, C> inputConnector;
  private MeshineryConnector<K, C> outputConnector;
  private DataInjectingExecutorService executorService;
  private Function<Throwable, DataContext> handleException = e -> null;

  private TaskData taskData = new TaskData().put(TaskDataProperties.TASK_NAME, taskName);
  private List<MeshineryProcessor<DataContext, DataContext>> processorList = new ArrayList<>();
  @Singular private List<ProcessorDecorator<C, C>> decorators = new ArrayList<>();

  private <I extends DataContext> MeshineryTaskFactory(
      MeshineryProcessor<I, C> newProcessor,
      List<MeshineryProcessor<DataContext, DataContext>> oldProcessorList,
      String name,
      MeshineryConnector inputConnector,
      MeshineryConnector outputConnector,
      DataInjectingExecutorService executorService,
      K eventKey,
      TaskData taskData,
      Function<Throwable, DataContext> handleException,
      long backoffTime
  ) {
    taskName = name;
    this.backoffTime = backoffTime;
    oldProcessorList.add((MeshineryProcessor<DataContext, DataContext>) newProcessor);
    this.processorList = oldProcessorList;
    this.inputConnector = inputConnector;
    this.outputConnector = outputConnector;
    this.executorService = executorService;
    this.inputKey = eventKey;
    this.taskData = taskData;
    this.handleException = handleException;
  }

  public static <K, C extends DataContext> MeshineryTaskFactory<K, C> builder() {
    return new MeshineryTaskFactory<>();
  }

  /**
   * Specifies the default output source of a task. This can be overridden by switching the context
   * or by providing another Outputsource to a write() call
   *
   * @param outputSource The source
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> defaultOutputSource(MeshineryConnector<K, C> outputSource) {
    return toBuilder()
        .outputConnector(outputSource)
        .taskData(taskData.put(TaskDataProperties.GRAPH_OUTPUT_SOURCE, outputSource.getName()))
        .build();
  }

  /**
   * The Inputsource of this MeshineryTask.
   *
   * @param inputSource The source
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> inputSource(MeshineryConnector<K, C> inputSource) {
    return toBuilder()
        .inputConnector(inputSource)
        .taskData(this.taskData.put(TaskDataProperties.GRAPH_INPUT_SOURCE, inputSource.getName()))
        .build();
  }

  /**
   * Reads from the inputsource with the provided key. Uses the executorService to query the inputData.
   *
   * @param inputKey        The Key to be used in the Inputsource
   * @param executorService The executorService to be used in the Inputsource
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> read(K inputKey, ExecutorService executorService) {
    return toBuilder()
        .inputKey(inputKey)
        .executorService(new DataInjectingExecutorService(inputKey.toString() + "-executor", executorService))
        .taskData(taskData.put(TaskDataProperties.GRAPH_INPUT_KEY, inputKey.toString()))
        .build();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTaskFactory<K, C> putData(String key, String value) {
    return toBuilder()
        .taskData(taskData.put(key, value))
        .build();
  }

  public MeshineryTaskFactory<K, C> readNewInput(K key, AccessingInputSource<K, C> newInputSource) {
    return addNewProcessor(new SignalingProcessor<>(newInputSource, key));
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
      MeshineryConnector<K, C> rightInputSource,
      K rightKey,
      int timeToLiveSeconds,
      BiFunction<C, C, C> combine
  ) {
    var name = "%s->%s__%s->%s".formatted(inputConnector.getName(), inputKey, rightInputSource.getName(), rightKey);

    return toBuilder()
        .inputConnector(
            new JoinedInnerInputSource<>(name, inputConnector, rightInputSource, rightKey, combine, timeToLiveSeconds))
        .taskData(taskData
            .put(TaskDataProperties.GRAPH_INPUT_SOURCE, rightInputSource.getName())
            .put(TaskDataProperties.GRAPH_INPUT_KEY, rightKey.toString()))
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
   * @param <N>             Type of the new Context
   * @return returns itself for builder pattern
   */
  public <N extends DataContext> MeshineryTaskFactory<K, N> contextSwitch(
      MeshineryConnector<K, N> newOutputSource,
      Function<C, N> map,
      List<ProcessorDecorator<N, N>> decorators
  ) {
    MeshineryProcessor<C, N> newProcessor = (context, ex) -> CompletableFuture.completedFuture(map.apply(context));

    var newTaskData = inputConnector.addToTaskData(
        taskData.put(TaskDataProperties.GRAPH_OUTPUT_SOURCE, newOutputSource.getName())
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
  public final MeshineryTaskFactory<K, C> write(K key, MeshineryConnector<K, C> outputSource) {
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
  public final MeshineryTaskFactory<K, C> write(K key, Predicate<C> writeIf, MeshineryConnector<K, C> outputSource) {
    var newTaskData = taskData.put(TaskDataProperties.GRAPH_OUTPUT_SOURCE, outputSource.getName())
        .put(TaskDataProperties.GRAPH_OUTPUT_KEY, key.toString());

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
        .taskData(taskData.put(TaskDataProperties.GRAPH_OUTPUT_KEY, key.toString()))
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
      MeshineryConnector<K, C> newOutputSource
  ) {
    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, keyFunction, newOutputSource))
        .toBuilder()
        .taskData(taskData.put(TaskDataProperties.GRAPH_OUTPUT_SOURCE, newOutputSource.getName()))
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
  public final MeshineryTaskFactory<K, C> exceptionHandler(Function<Throwable, DataContext> handleError) {
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

  private <N extends DataContext> MeshineryTaskFactory<K, N> addNewProcessor(MeshineryProcessor<C, N> newProcessor) {
    return new MeshineryTaskFactory<>(
        newProcessor,
        processorList,
        taskName,
        inputConnector,
        outputConnector,
        executorService,
        inputKey,
        taskData,
        handleException,
        backoffTime
    ).toBuilder()
        .taskData(newProcessor.addToTaskData(
            taskData.put(TaskDataProperties.GRAPH_PROCESSOR, newProcessor.getClass().getSimpleName())))
        .build();
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<K, C> build() {
    Objects.requireNonNull(inputKey, "No input key specified");
    Objects.requireNonNull(inputConnector, "Input source not specified");

    return new MeshineryTask<>(
        backoffTime,
        inputKey,
        taskName,
        inputConnector.addToTaskData(taskData),
        inputConnector,
        outputConnector,
        executorService,
        handleException,
        processorList
    );
  }
}
