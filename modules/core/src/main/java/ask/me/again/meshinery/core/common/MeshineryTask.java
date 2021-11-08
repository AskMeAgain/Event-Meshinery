package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.processors.DynamicOutputProcessor;
import ask.me.again.meshinery.core.processors.LambdaProcessor;
import ask.me.again.meshinery.core.processors.OutputProcessor;
import ask.me.again.meshinery.core.processors.StopProcessor;
import ask.me.again.meshinery.core.source.JoinedInputSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Getter;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryTask<K, C extends Context> {

  @Getter
  List<MeshineryProcessor<Context, Context>> processorList = new ArrayList<>();

  @Getter
  List<K> outputKeys = new ArrayList<>();

  @Getter
  ExecutorService executorService;

  @Getter
  List<K> inputKeys;

  @Getter
  OutputSource<K, C> defaultOutputSource;

  InputSource<K, C> inputSource;

  @Getter
  String taskName;

  private MeshineryTask() {
  }


  private <Input extends Context> MeshineryTask(
      MeshineryProcessor<Input, C> newProcessor,
      List<MeshineryProcessor<Context, Context>> oldProcessorList,
      String name,
      InputSource inputSource,
      OutputSource defaultOutputSource,
      ExecutorService executorService,
      List<K> outputKeys,
      List<K> inputKey
  ) {
    taskName = name;
    oldProcessorList.add((MeshineryProcessor<Context, Context>) newProcessor);
    this.processorList = oldProcessorList;
    this.inputSource = inputSource;
    this.defaultOutputSource = defaultOutputSource;
    this.outputKeys = outputKeys;
    this.executorService = executorService;
    this.inputKeys = inputKey;
  }

  public static <Key, Output extends Context> MeshineryTask<Key, Output> builder() {
    return new MeshineryTask<>();
  }

  List<C> getInputValues() {
    return inputSource.getInputs(inputKeys.get(0));
  }

  /**
   * Specifies the default output source of a task. This can be overridden by switching the context
   * or by providing another Outputsource to a write() call
   *
   * @param outputSource The source
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> defaultOutputSource(OutputSource<K, C> outputSource) {
    this.defaultOutputSource = outputSource;
    return this;
  }

  /**
   * The Inputsource of this MeshineryTask.
   *
   * @param inputSource The source
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> inputSource(InputSource<K, C> inputSource) {
    this.inputSource = inputSource;
    return this;
  }

  /**
   * Reads from the inputsource with the provided key. Uses the executorService to query the inputData.
   *
   * @param inputKey        The Key to be used in the Inputsource
   * @param executorService The executorService to be used in the Inputsource
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> read(K inputKey, ExecutorService executorService) {
    this.executorService = executorService;
    this.inputKeys = new ArrayList<>(List.of(inputKey));
    return this;
  }

  public MeshineryTask<K, C> joinOn(InputSource<K, C> rightInputSource, K rightKey, BiFunction<C, C, C> combine) {
    this.inputKeys.add(rightKey);
    this.inputSource = new JoinedInputSource<>(inputSource, rightInputSource, rightKey, combine);
    return this;
  }

  /**
   * Method used to set a name for a MeshineryTask. This name will be used for the drawer methods and general logging
   *
   * @param name the name
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> taskName(String name) {
    taskName = name;
    return this;
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
    return new MeshineryTask<>(
        new LambdaProcessor<>(map),
        processorList,
        taskName,
        inputSource,
        newOutputSource,
        executorService,
        outputKeys,
        inputKeys
    );
  }

  /**
   * Adds a processor to a MeshineryTask, which stops the processing of a single event when the provided Predicate
   * returns true. Equivalent to Java Streams .filter() method
   *
   * @param stopIf Predicate to use
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> stopIf(Predicate<C> stopIf) {
    return new MeshineryTask<>(
        new StopProcessor<>(stopIf),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKeys
    );
  }

  /**
   * Adds a new processor the the list. Will be run in order
   *
   * @param processor will be added
   * @return returns itself for builder pattern
   */
  public MeshineryTask<K, C> process(MeshineryProcessor<C, C> processor) {
    return new MeshineryTask<>(
        processor,
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKeys
    );
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
    outputKeys.add(key);

    return new MeshineryTask<>(
        new OutputProcessor<>(key, writeIf, outputSource),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKeys
    );
  }

  /**
   * Writes an event if a predicate returns true and uses a dynamic KeyFunction. Uses the defaultOutputSource.
   *
   * @param keyFunction Keyfunction to be used
   * @param writeIf     predicate to be used
   * @return returns itself for builder pattern
   */
  public final MeshineryTask<K, C> write(Function<C, K> keyFunction, Predicate<C> writeIf) {
    return new MeshineryTask<>(
        new DynamicOutputProcessor<>(writeIf, keyFunction, defaultOutputSource),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKeys
    );
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
    return new MeshineryTask<>(
        new DynamicOutputProcessor<>(writeIf, keyFunction, newOutputSource),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKeys
    );
  }
}
