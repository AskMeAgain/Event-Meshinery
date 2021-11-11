package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.processors.DynamicOutputProcessor;
import ask.me.again.meshinery.core.processors.LambdaProcessor;
import ask.me.again.meshinery.core.processors.OutputProcessor;
import ask.me.again.meshinery.core.processors.StopProcessor;
import ask.me.again.meshinery.core.source.JoinedInputSource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.With;

/**
 * A Meshinery task consists of an input source, a list of processors and multiple output sources.
 */
@NoArgsConstructor
@AllArgsConstructor
public class MeshineryTask<K, C extends Context> {

  @Getter private List<MeshineryProcessor<Context, Context>> processorList = new ArrayList<>();
  @Getter @With private GraphData<K> graphData = new GraphData<>();

  @Getter @With private Function<Throwable, Context> handleException = exception -> null;
  @Getter @With private OutputSource<K, C> defaultOutputSource;
  @Getter @With private MdcInjectingExecutorService executorService;
  @Getter @With private String taskName = "Default Task";
  @Getter @With private K inputKey;

  @With private InputSource<K, C> inputSource;
  @With private long backoffTime = 0;

  private Instant nextExecution = Instant.now();

  private <I extends Context> MeshineryTask(
      MeshineryProcessor<I, C> newProcessor,
      List<MeshineryProcessor<Context, Context>> oldProcessorList,
      String name,
      InputSource inputSource,
      OutputSource defaultOutputSource,
      MdcInjectingExecutorService executorService,
      K inputKey,
      GraphData<K> graphData,
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
    this.graphData = graphData;
    this.handleException = handleException;
  }

  public static <K, C extends Context> MeshineryTask<K, C> builder() {
    return new MeshineryTask<>();
  }

  /**
   * Pulls the next batch of data from the input source. Keeps the backoff period in mind, which in this case returns
   * empty list and doesnt poll the source
   *
   * @return returns itself for builder pattern
   */
  public List<C> getInputValues() {
    var now = Instant.now();
    if (now.isAfter(nextExecution)) {
      nextExecution = now.plusMillis(backoffTime);
      return inputSource.getInputs(inputKey);
    } else {
      return Collections.emptyList();
    }
  }

  /**
   * Specifies the default output source of a task. This can be overridden by switching the context
   * or by providing another Outputsource to a write() call
   *
   * @param outputSource The source
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> defaultOutputSource(OutputSource<K, C> outputSource) {
    return this.withDefaultOutputSource(outputSource);
  }

  /**
   * The Inputsource of this MeshineryTask.
   *
   * @param inputSource The source
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> inputSource(InputSource<K, C> inputSource) {
    return this.withInputSource(inputSource);
  }

  /**
   * Reads from the inputsource with the provided key. Uses the executorService to query the inputData.
   *
   * @param inputKey        The Key to be used in the Inputsource
   * @param executorService The executorService to be used in the Inputsource
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> read(K inputKey, ExecutorService executorService) {
    return this.withGraphData(this.getGraphData().addInputKey(inputKey))
        .withExecutorService(new MdcInjectingExecutorService(executorService))
        .withInputKey(inputKey);
  }

  /**
   * Adds another inputsource which gets joined on. As join key the context Id will be used.
   *
   * @param rightInputSource the right side of the join sources
   * @param rightKey         the key of the right source
   * @param combine          the combine method used to be applied to the join results
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> joinOn(InputSource<K, C> rightInputSource, K rightKey, BiFunction<C, C, C> combine) {
    return this.withGraphData(this.getGraphData().addInputKey(rightKey))
        .withInputSource(new JoinedInputSource<>(inputSource, rightInputSource, rightKey, combine));
  }

  /**
   * Method used to set a name for a MeshineryTask. This name will be used for the drawer methods and general logging
   *
   * @param name the name
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> taskName(String name) {
    return this.withTaskName(name);
  }

  /**
   * Context switch in a task. Needs a mapping method and a new defaultOutputsource to write to.
   *
   * @param newOutputSource new Outputsource to be used further down in the processor list
   * @param map             mapping function from one Context to another
   * @param <N>             Type of the new Context
   * @return returns itself for builder pattern
   */
  public <N extends Context> MeshineryTask<K, N> contextSwitch(OutputSource<K, N> newOutputSource, Function<C, N> map) {
    return addNewProcessor(new LambdaProcessor<>(map)).withDefaultOutputSource(newOutputSource);
  }

  /**
   * Adds a processor to a MeshineryTask, which stops the processing of a single event when the provided Predicate
   * returns true. Equivalent to Java Streams .filter() method
   *
   * @param stopIf Predicate to use
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> stopIf(Predicate<C> stopIf) {
    return addNewProcessor(new StopProcessor<>(stopIf));
  }

  /**
   * Adds a new processor the the list. Will be run in order
   *
   * @param processor will be added
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> process(MeshineryProcessor<C, C> processor) {
    return addNewProcessor(processor);
  }

  /**
   * Writes an event to an OutputSource.
   *
   * @param key          the key to be used in the OutputSource
   * @param outputSource the OutputSource which will be used
   * @return returns itself for builder pattern
   */
  public final MeshineryTask<K, C> write(K key, OutputSource<K, C> outputSource) {
    return write(key, x -> true, outputSource);
  }

  /**
   * Writes an event for each provided key to an OutputSource.
   *
   * @param keys keys to be used
   * @return returns itself for builder pattern
   */
  @SafeVarargs
  public final MeshineryTask<K, C> write(K... keys) {
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
  public final MeshineryTask<K, C> write(K key, Predicate<C> writeIf, OutputSource<K, C> outputSource) {
    return this.withGraphData(this.getGraphData().addOutputKey(key))
        .addNewProcessor(new OutputProcessor<>(key, writeIf, outputSource));
  }

  /**
   * Writes an event if a predicate returns true and uses a dynamic KeyFunction. Uses the defaultOutputSource.
   *
   * @param keyFunction Keyfunction to be used
   * @param writeIf     predicate to be used
   * @return returns itself for builder pattern
   */
  public final MeshineryTask<K, C> write(Function<C, K> keyFunction, Predicate<C> writeIf) {
    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, keyFunction, defaultOutputSource));
  }

  /**
   * Writes an event if a predicate returns true and uses a dynamic KeyFunction. Used the provided OutputSource
   *
   * @param keyFunction     Keyfunction to be used
   * @param writeIf         predicate to be used
   * @param newOutputSource Outputsource to be used
   * @return returns itself for builder pattern
   */
  public final MeshineryTask<K, C> write(
      Function<C, K> keyFunction,
      Predicate<C> writeIf,
      OutputSource<K, C> newOutputSource
  ) {
    return addNewProcessor(new DynamicOutputProcessor<>(writeIf, keyFunction, newOutputSource));
  }

  /**
   * Registers a method handler, which is getting executed when an exception happens INSIDE the completable future.
   * Allows to return a different value instead. Passes the handleError function to the .handle() method of
   * completable future
   *
   * @param handleError The method which will be passed to the .handle() method of completable future
   * @return returns itself for builder pattern
   */
  public final MeshineryTask<K, C> exceptionHandler(Function<Throwable, Context> handleError) {
    return this.withHandleException(handleError);
  }

  /**
   * Adds an interval between input source polling
   *
   * @param milliSeconds the time in milliseconds
   * @return returns itself for builder pattern
   */
  public final MeshineryTask<K, C> backoffTime(long milliSeconds) {
    return this.withBackoffTime(milliSeconds);
  }

  private <N extends Context> MeshineryTask<K, N> addNewProcessor(MeshineryProcessor<C, N> newProcessor) {
    return new MeshineryTask<>(
        newProcessor,
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        inputKey,
        graphData,
        handleException,
        backoffTime
    );
  }
}
