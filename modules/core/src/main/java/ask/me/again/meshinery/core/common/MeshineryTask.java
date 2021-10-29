package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.processors.DynamicOutputProcessor;
import ask.me.again.meshinery.core.processors.LambdaProcessor;
import ask.me.again.meshinery.core.processors.OutputProcessor;
import ask.me.again.meshinery.core.processors.StopProcessor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;


public class MeshineryTask<Key, Output extends Context> {

  @Getter
  List<MeshineryProcessor<Context, Context>> processorList = new ArrayList<>();

  @Getter
  List<Key> outputKeys = new ArrayList<>();

  @Getter
  ExecutorService executorService;

  @Getter
  Key inputKey;

  @Getter
  OutputSource<Key, Output> defaultOutputSource;

  @Getter
  InputSource<Key, Output> inputSource;

  @Getter
  String taskName;


  public static <Key, Output extends Context> MeshineryTask<Key, Output> builder() {
    return new MeshineryTask<>();
  }

  private MeshineryTask() {
  }

  private <Input extends Context> MeshineryTask(MeshineryProcessor<Input, Output> newProcessor,
                                                List<MeshineryProcessor<Context, Context>> oldProcessorList,
                                                String name,
                                                InputSource inputSource,
                                                OutputSource defaultOutputSource,
                                                ExecutorService executorService,
                                                List<Key> outputKeys,
                                                Key inputKey
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

  public MeshineryTask<Key, Output> defaultOutputSource(OutputSource<Key, Output> outputSource) {
    this.defaultOutputSource = outputSource;
    return this;
  }

  public MeshineryTask<Key, Output> inputSource(InputSource<Key, Output> inputSource) {
    this.inputSource = inputSource;
    return this;
  }

  public MeshineryTask<Key, Output> read(Key inputKey, ExecutorService executorService) {
    this.executorService = executorService;
    this.inputKey = inputKey;
    return this;
  }

  public MeshineryTask<Key, Output> taskName(String name) {
    taskName = name;
    return this;
  }

  public <N extends Context> MeshineryTask<Key, N> contextSwitch(OutputSource<Key, N> newOutputSource, Function<Output, N> map) {
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

  public MeshineryTask<Key, Output> stopIf(Function<Output, Boolean> stopIf) {
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

  public MeshineryTask<Key, Output> process(MeshineryProcessor<Output, Output> processor) {
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

  public final MeshineryTask<Key, Output> write(Key key, OutputSource<Key, Output> outputSource) {
    return write(key, x -> true, outputSource);
  }

  @SafeVarargs
  public final MeshineryTask<Key, Output> write(Key... keys) {
    var temp = this;
    for (Key key : keys) {
      temp = temp.write(key, x -> true, defaultOutputSource);
    }
    return temp;
  }

  public final MeshineryTask<Key, Output> write(Key key, Function<Output, Boolean> writeIf, OutputSource<Key, Output> oSource) {
    outputKeys.add(key);

    return new MeshineryTask<>(
        new OutputProcessor<>(key, writeIf, oSource),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  public final MeshineryTask<Key, Output> write(Function<Output, Key> keyFunction, Function<Output, Boolean> writeIf) {
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

  public final MeshineryTask<Key, Output> write(Function<Output, Key> keyFunction, Function<Output, Boolean> writeIf, OutputSource<Key, Output> oSource) {
    return new MeshineryTask<>(
        new DynamicOutputProcessor<>(writeIf, keyFunction, oSource),
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
