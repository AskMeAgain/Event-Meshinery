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

  public <N extends Context> MeshineryTask<Key, N> contextSwitch(Function<Output, N> map) {
    return new MeshineryTask<Key, N>(
        new LambdaProcessor<Output, N>(map),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
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

  public MeshineryTask<Key, Output> write(Key output, OutputSource<Key, Output> outputSource) {
    return write(output, x -> true, outputSource);
  }

  public MeshineryTask<Key, Output> write(Key input) {
    return write(input, x -> true, defaultOutputSource);
  }

  public MeshineryTask<Key, Output> write(Key input, Function<Output, Boolean> writeIf, OutputSource<Key, Output> oSource) {
    outputKeys.add(input);

    return new MeshineryTask<>(
        new OutputProcessor<>(input, writeIf, oSource),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  public MeshineryTask<Key, Output> write(Function<Output, Key> inputMethod, Function<Output, Boolean> writeIf) {
    return new MeshineryTask<>(
        new DynamicOutputProcessor<>(writeIf, inputMethod, defaultOutputSource),
        processorList,
        taskName,
        inputSource,
        defaultOutputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  public MeshineryTask<Key, Output> write(Function<Output, Key> inputMethod, Function<Output, Boolean> writeIf, OutputSource<Key, Output> oSource) {
    return new MeshineryTask<>(
        new DynamicOutputProcessor<>(writeIf, inputMethod, oSource),
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
