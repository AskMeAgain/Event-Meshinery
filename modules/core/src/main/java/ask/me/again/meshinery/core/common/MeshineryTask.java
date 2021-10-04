package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.processors.DynamicOutputProcessor;
import ask.me.again.meshinery.core.processors.OutputProcessor;
import ask.me.again.meshinery.core.processors.StopProcessor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;


public class MeshineryTask<K, I, O> {

  @Getter
  List<MeshineryProcessor<Object, Object>> processorList = new ArrayList<>();

  @Getter
  List<K> outputKeys = new ArrayList<>();

  @Getter
  ExecutorService executorService;

  @Getter
  K inputKey;

  @Getter
  OutputSource<K, O> outputSource;

  @Getter
  InputSource<K, I> inputSource;

  @Getter
  String taskName;

  public MeshineryTask() {
  }

  private MeshineryTask(MeshineryProcessor<I, O> newProcessor,
                        List<MeshineryProcessor<Object, Object>> oldProcessorList,
                        String name,
                        InputSource inputSource,
                        OutputSource outputSource,
                        ExecutorService executorService,
                        List<K> outputKeys,
                        K inputKey
  ) {
    taskName = name;
    oldProcessorList.add((MeshineryProcessor<Object, Object>) newProcessor);
    this.processorList = oldProcessorList;
    this.inputSource = inputSource;
    this.outputSource = outputSource;
    this.outputKeys = outputKeys;
    this.executorService = executorService;
    this.inputKey = inputKey;
  }

  public MeshineryTask<K, I, O> outputSource(OutputSource<K, O> outputSource) {
    this.outputSource = outputSource;
    return this;
  }

  public MeshineryTask<K, I, O> inputSource(InputSource<K, I> inputSource) {
    this.inputSource = inputSource;
    return this;
  }

  public MeshineryTask<K, I, O> read(K inputKey, ExecutorService executorService) {
    this.executorService = executorService;
    this.inputKey = inputKey;
    return this;
  }

  public MeshineryTask<K, I, O> taskName(String name) {
    taskName = name;
    return this;
  }

  public MeshineryTask<K, O, O> stopIf(Function<O, Boolean> stopIf) {
    return new MeshineryTask<>(new StopProcessor<>(stopIf),
        processorList,
        taskName,
        inputSource,
        outputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  public <N> MeshineryTask<K, O, N> process(MeshineryProcessor<O, N> processor) {
    return new MeshineryTask<>(
        processor,
        processorList,
        taskName,
        inputSource,
        outputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  public MeshineryTask<K, O, O> write(K input) {
    return write(input, x -> true);
  }

  public MeshineryTask<K, O, O> write(K input, Function<O, Boolean> writeIf) {
    outputKeys.add(input);
    var processor = new OutputProcessor<>(input, writeIf, outputSource);
    return new MeshineryTask<>(
        processor,
        processorList,
        taskName,
        inputSource,
        outputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }

  public MeshineryTask<K, O, O> write(Function<O, K> inputMethod, Function<O, Boolean> writeIf) {
    var processor = new DynamicOutputProcessor<>(writeIf, inputMethod, outputSource);
    return new MeshineryTask<>(
        processor,
        processorList,
        taskName,
        inputSource,
        outputSource,
        executorService,
        outputKeys,
        inputKey
    );
  }
}
