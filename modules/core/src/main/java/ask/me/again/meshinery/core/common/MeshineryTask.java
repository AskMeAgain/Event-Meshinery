package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.processors.OutputProcessor;
import ask.me.again.meshinery.core.processors.StopProcessor;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;


@Value
@Builder
public class MeshineryTask<K, C extends Context> {

  List<MeshineryProcessor<C>> processorList;
  List<K> outputKeys;

  ExecutorService executorService;

  K inputKey;

  OutputSource<K, C> outputSource;

  InputSource<K, C> inputSource;

  String taskName;

  public static class MeshineryTaskBuilder<K, C extends Context> {

    private MeshineryTaskBuilder<K, C> executorService(ExecutorService executorService) {
      return this;
    }

    private MeshineryTaskBuilder<K, C> inputKey(K inputKey) {
      return this;
    }

    private MeshineryTaskBuilder<K, C> processorList(List<MeshineryProcessor<C>> processorList) {
      return this;
    }

    public MeshineryTaskBuilder<K, C> read(K inputKey, ExecutorService executor) {

      this.processorList = new ArrayList<>();
      this.outputKeys = new ArrayList<>();
      this.inputKey = inputKey;
      this.executorService = executor;

      return this;
    }

    public MeshineryTaskBuilder<K, C> stopIf(Function<C, Boolean> stopIf) {
      processorList.add(new StopProcessor<>(stopIf));
      return this;
    }

    public MeshineryTaskBuilder<K, C> write(K input) {
      return write(input, x -> true);
    }

    public MeshineryTaskBuilder<K, C> write(K input, Function<C, Boolean> writeIf) {
      outputKeys.add(input);
      processorList.add(new OutputProcessor<>(writeIf, input, outputSource));
      return this;
    }

    public MeshineryTaskBuilder<K, C> process(MeshineryProcessor<C> processor) {
      processorList.add(processor);
      return this;
    }
  }
}
