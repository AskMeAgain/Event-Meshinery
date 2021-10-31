package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.processors.DynamicOutputProcessor;
import ask.me.again.meshinery.core.processors.LambdaProcessor;
import ask.me.again.meshinery.core.processors.OutputProcessor;
import ask.me.again.meshinery.core.processors.StopProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
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
  K inputKey;

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
      K inputKey
  ) {
    taskName = name;
    oldProcessorList.add((MeshineryProcessor<Context, Context>) newProcessor);
    this.processorList = oldProcessorList;
    this.inputSource = inputSource;
    this.defaultOutputSource = defaultOutputSource;
    this.outputKeys = outputKeys;
    this.executorService = executorService;
    this.inputKey = inputKey;
  }

  public static <Key, Output extends Context> MeshineryTask<Key, Output> builder() {
    return new MeshineryTask<>();
  }

  List<C> getInputValues() {
    return inputSource.getInputs(inputKey);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<K, C> defaultOutputSource(OutputSource<K, C> outputSource) {
    this.defaultOutputSource = outputSource;
    return this;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<K, C> inputSource(InputSource<K, C> inputSource) {
    this.inputSource = inputSource;
    return this;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<K, C> read(K inputKey, ExecutorService executorService) {
    this.executorService = executorService;
    this.inputKey = inputKey;
    return this;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<K, C> taskName(String name) {
    taskName = name;
    return this;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public <N extends Context> MeshineryTask<K, N> contextSwitch(
      OutputSource<K, N> newOutputSource, Function<C, N> map
  ) {
    return new MeshineryTask<>(
        new LambdaProcessor<>(map),
        processorList,
        taskName,
        inputSource,
        newOutputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<K, C> stopIf(Predicate<C> stopIf) {
    return new MeshineryTask<>(
        new StopProcessor<>(stopIf),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<K, C> process(MeshineryProcessor<C, C> processor) {
    return new MeshineryTask<>(
        processor,
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public final MeshineryTask<K, C> write(K key, OutputSource<K, C> outputSource) {
    return write(key, x -> true, outputSource);
  }

  @SafeVarargs
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public final MeshineryTask<K, C> write(K... keys) {
    var temp = this;
    for (K key : keys) {
      temp = temp.write(key, x -> true, defaultOutputSource);
    }
    return temp;
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public final MeshineryTask<K, C> write(
      K key, Function<C, Boolean> writeIf, OutputSource<K, C> outputSource
  ) {
    outputKeys.add(key);

    return new MeshineryTask<>(
        new OutputProcessor<>(key, writeIf, outputSource),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public final MeshineryTask<K, C> write(Function<C, K> keyFunction, Function<C, Boolean> writeIf) {
    return new MeshineryTask<>(
        new DynamicOutputProcessor<>(writeIf, keyFunction, defaultOutputSource),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public final MeshineryTask<K, C> write(
      Function<C, K> keyFunction, Function<C, Boolean> writeIf, OutputSource<K, C> newOutputSource
  ) {
    return new MeshineryTask<>(
        new DynamicOutputProcessor<>(writeIf, keyFunction, newOutputSource),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }
}
