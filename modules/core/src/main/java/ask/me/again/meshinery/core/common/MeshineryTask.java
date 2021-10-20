package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.processors.DynamicOutputProcessor;
import ask.me.again.meshinery.core.processors.OutputProcessor;
import ask.me.again.meshinery.core.processors.StopProcessor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;


public class MeshineryTask<Key, Input extends Context, Output extends Context> {

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
  InputSource<Key, Input> inputSource;

  @Getter
  String taskName;

  public static <K, I extends Context> MeshineryTask<K, I, I> builder(){
    return new MeshineryTask<>();
  }

  public MeshineryTask() {
  }

  private MeshineryTask(MeshineryProcessor<Input, Output> newProcessor,
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

  public MeshineryTask<Key, Input, Output> defaultOutputSource(OutputSource<Key, Output> outputSource) {
    this.defaultOutputSource = outputSource;
    return this;
  }

  public MeshineryTask<Key, Input, Output> inputSource(InputSource<Key, Input> inputSource) {
    this.inputSource = inputSource;
    return this;
  }

  public MeshineryTask<Key, Input, Output> read(Key inputKey, ExecutorService executorService) {
    this.executorService = executorService;
    this.inputKey = inputKey;
    return this;
  }

  public MeshineryTask<Key, Input, Output> taskName(String name) {
    taskName = name;
    return this;
  }

  public MeshineryTask<Key, Output, Output> stopIf(Function<Output, Boolean> stopIf) {
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

  public <N extends Context> MeshineryTask<Key, Output, N> process(MeshineryProcessor<Output, N> processor) {
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

  public MeshineryTask<Key, Output, Output> write(Key output, OutputSource<Key, Output> outputSource) {
    return write(output, x -> true, outputSource);
  }

  public MeshineryTask<Key, Output, Output> write(Key input) {
    return write(input, x -> true, defaultOutputSource);
  }

  public MeshineryTask<Key, Output, Output> write(Key input, Function<Output, Boolean> writeIf, OutputSource<Key, Output> oSource) {
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

  public MeshineryTask<Key, Output, Output> write(Function<Output, Key> inputMethod, Function<Output, Boolean> writeIf) {
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

  public MeshineryTask<Key, Output, Output> write(Function<Output, Key> inputMethod, Function<Output, Boolean> writeIf, OutputSource<Key, Output> oSource) {
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
