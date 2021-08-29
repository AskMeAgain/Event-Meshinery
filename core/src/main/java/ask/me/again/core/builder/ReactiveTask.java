package ask.me.again.core.builder;

import ask.me.again.core.common.Context;
import ask.me.again.core.common.OutputSource;
import ask.me.again.core.common.ReactiveProcessor;
import ask.me.again.core.processors.PassthroughProcessor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;


@Builder
public class ReactiveTask<K, C extends Context> {

  @Getter
  List<ReactiveProcessor<C>> processorList;

  @Getter
  ExecutorService executorService;

  @Getter
  K inputKey;

  OutputSource<K, C> outputSource;

  String taskName;

  public static class ReactiveTaskBuilder<K, C extends Context> {

    private ReactiveTaskBuilder<K, C> executorService(ExecutorService executorService) {
      return this;
    }

    private ReactiveTaskBuilder<K, C> inputKey(K inputKey) {
      return this;
    }

    private ReactiveTaskBuilder<K, C> processorList(List<ReactiveProcessor<C>> processorList) {
      return this;
    }

    public ReactiveTaskBuilder<K, C> read(K inputKey, ExecutorService executor) {

      this.processorList = new ArrayList<>();
      this.inputKey = inputKey;
      this.executorService = executor;

      return this;
    }

    public ReactiveTaskBuilder<K, C> write(K input) {
      processorList.add(new PassthroughProcessor<K, C>(input, outputSource));
      return this;
    }

    public ReactiveTaskBuilder<K, C> process(ReactiveProcessor<C> processor) {
      processorList.add(processor);
      return this;
    }
  }
}
