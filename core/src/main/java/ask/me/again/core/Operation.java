package ask.me.again.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Operation<C extends Context> {

  String read;
  String write;
  ReactiveProcessor<C> processor;

}
