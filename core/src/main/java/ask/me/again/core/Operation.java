package ask.me.again.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Operation {

  String topicName;
  ReactiveProcessor<? extends Context> processor;

  public String toString() {
    if(topicName != null)
      return topicName;

    return "processor";
  }
}
