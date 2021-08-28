package ask.me.again.core.worker;

import ask.me.again.core.common.Context;
import ask.me.again.core.common.ReactiveProcessor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ReactiveTask<C extends Context> {

  @Getter
  private final List<ReactiveProcessor<C>> processorList = new ArrayList<>();

  public void add(ReactiveProcessor<C> processor) {
    processorList.add(processor);
  }

}
