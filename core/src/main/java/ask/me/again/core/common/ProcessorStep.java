package ask.me.again.core.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProcessorStep<C extends Context> {

  String name;
  String read;
  String write;
  ReactiveProcessor<C> processor;

}
