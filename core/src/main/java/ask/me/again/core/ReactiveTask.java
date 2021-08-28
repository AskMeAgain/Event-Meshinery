package ask.me.again.core;

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
