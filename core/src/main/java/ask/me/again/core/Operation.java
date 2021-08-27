package ask.me.again.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Operation<C extends Context> {

  String topicName;
  boolean passthrough;
  ReactiveProcessor<C> processor;

  public String toString() {
    if(topicName != null)
      return topicName;

    return "processor";
  }
}
