package ask.me.again.meshinery.core.task;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MdcInjectingExecutorService;
import ask.me.again.meshinery.core.common.MeshineryProcessor;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.common.TaskData;
import ask.me.again.meshinery.core.processors.DynamicOutputProcessor;
import ask.me.again.meshinery.core.processors.OutputProcessor;
import ask.me.again.meshinery.core.processors.StopProcessor;
import ask.me.again.meshinery.core.source.JoinedInputSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MeshineryTaskFactory<K, C extends Context> {

  private K inputKey;
  private String taskName;
  private long backoffTime;
  private InputSource<K, C> inputSource;
  private OutputSource<K, C> defaultOutputSource;
  private MdcInjectingExecutorService executorService;
  private Function<Throwable, Context> handleException;

  private TaskData taskData = new TaskData();
  private List<MeshineryProcessor<Context, Context>> processorList = new ArrayList<>();

  private <I extends Context> MeshineryTaskFactory(
      MeshineryProcessor<I, C> newProcessor,
      List<MeshineryProcessor<Context, Context>> oldProcessorList,
      String name,
      InputSource inputSource,
      OutputSource defaultOutputSource,
      MdcInjectingExecutorService executorService,
      K inputKey,
      TaskData taskData,
      Function<Throwable, Context> handleException,
      long backoffTime
  ) {
    taskName = name;
    this.backoffTime = backoffTime;
    oldProcessorList.add((MeshineryProcessor<Context, Context>) newProcessor);
    this.processorList = oldProcessorList;
    this.inputSource = inputSource;
    this.defaultOutputSource = defaultOutputSource;
    this.executorService = executorService;
    this.inputKey = inputKey;
    this.taskData = taskData;
    this.handleException = handleException;
  }

  public static <K, C extends Context> MeshineryTaskFactory<K, C> builder() {
    return new MeshineryTaskFactory<>();
  }

  /**
   * Specifies the default output source of a task. This can be overridden by switching the context
   * or by providing another Outputsource to a write() call
   *
   * @param outputSource The source
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> defaultOutputSource(OutputSource<K, C> outputSource) {
    return toBuilder()
        .defaultOutputSource(outputSource)
        .taskData(taskData.appendToList("graph.outputSource", outputSource.getName()))
        .build();
  }

  /**
   * The Inputsource of this MeshineryTask.
   *
   * @param inputSource The source
   * @return returns itself for builder pattern
   */
  public MeshineryTaskFactory<K, C> inputSource(InputSource<K, C> inputSource) {
    return toBuilder()
        .inputSource(inputSource)
        .taskData(this.taskData.appendToList("graph.inputSource", inputSource.getName()))
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
        .executorService(new MdcInjectingExecutorService(executorService))
        .taskData(taskData.appendToList("graph.inputKey", inputKey.toString()))
        .build();
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
      InputSource<K, C> rightInputSource, K rightKey, BiFunction<C, C, C> combine
  ) {
    var name = "%s->%s__%s->%s".formatted(inputSource.getName(), inputKey, rightInputSource.getName(), rightKey);
    var newTaskData = taskData.appendToList("graph.inputSource", rightInputSource.getName())
        .appendToList("graph.inputKey", rightKey.toString());

    return toBuilder()
        .inputSource(new JoinedInputSource<>(name, inputSource, rightInputSource, rightKey, combine))
        .taskData(newTaskData)
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
        .taskData(taskData.put("task.name", name))
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
  public <N extends Context> MeshineryTaskFactory<K, N> contextSwitch(
      OutputSource<K, N> newOutputSource, Function<C, N> map
  ) {
    MeshineryProcessor<C, N> newProcessor = (context, ex) -> CompletableFuture.completedFuture(map.apply(context));
    return addNewProcessor(newProcessor)
        .toBuilder()
        .defaultOutputSource(newOutputSource)
        .taskData(taskData.appendToList("graph.outputSource", newOutputSource.getName()))
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
    return addNewProcessor(processor);
  }

  /**
   * Writes an event to an OutputSource.
   *
   * @param key          the key to be used in the OutputSource
   * @param outputSource the OutputSource which will be used
   * @return returns itself for builder pattern
   */
  public final MeshineryTaskFactory<K, C> write(K key, OutputSource<K, C> outputSource) {
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
      temp = temp.write(key, x -> true, defaultOutputSource);
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
  public final MeshineryTaskFactory<K, C> write(K key, Predicate<C> writeIf, OutputSource<K, C> outputSource) {
    var newTaskData = taskData.appendToList("graph.outputSource", outputSource.getName())
        .appendToList("graph.outputKey", key.toString());

    return addNewProcessor(new OutputProcessor<>(key, writeIf, outputSource))
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
    var newTaskData = taskData.appendToList("graph.outputKey", key.toString());

    return addNewProcessor(new OutputProcessor<>(key, writeIf, defaultOutputSource))
        .toBuilder()
        .taskData(newTaskData)
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
    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, keyFunction, defaultOutputSource));
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
      OutputSource<K, C> newOutputSource
  ) {
    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, keyFunction, newOutputSource))
        .toBuilder()
        .taskData(taskData.appendToList("graph.outputSource", newOutputSource.getName()))
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
  public final MeshineryTaskFactory<K, C> exceptionHandler(Function<Throwable, Context> handleError) {
    return toBuilder()
        .handleException(handleError)
        .build();
  }

  /**
   * Adds an interval between input source polling
   *
   * @param milliSeconds the time in milliseconds
   * @return returns itself for builder pattern
   */
  public final MeshineryTaskFactory<K, C> backoffTime(long milliSeconds) {
    return toBuilder()
        .backoffTime(milliSeconds)
        .build();
  }

  private <N extends Context> MeshineryTaskFactory<K, N> addNewProcessor(MeshineryProcessor<C, N> newProcessor) {
    return new MeshineryTaskFactory<>(
        newProcessor,
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        inputKey,
        taskData,
        handleException,
        backoffTime
    ).toBuilder()
        .taskData(taskData.appendToList("graph.processor", newProcessor.toString()))
        .build();
  }

  public MeshineryTask<K, C> build() {
    return new MeshineryTask<>(
        backoffTime,
        inputKey,
        taskName,
        taskData,
        inputSource,
        defaultOutputSource,
        executorService,
        handleException,
        processorList
    );
  }


}
