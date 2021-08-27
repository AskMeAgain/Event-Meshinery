package ask.me.again.core;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ReactiveKafka<C extends Context> {

  @Getter
  private final List<Operation> inputs;

  public ReactiveKafka() {
    this(null);
  }

  public ReactiveKafka(ReactiveKafka<C> reactiveKafka) {

    if (reactiveKafka == null) {
      inputs = new ArrayList<>();
    } else {
      inputs = reactiveKafka.getInputs();
    }
  }

  public ReactiveKafka<C> topic(String input) {
    inputs.add(Operation.builder()
      .topicName(input)
      .build());
    return new ReactiveKafka<>(this);
  }

  public <P extends ReactiveProcessor<C>> ReactiveKafka<C> run(P processor) {
    inputs.add(Operation.builder()
      .processor(processor)
      .build());
    return new ReactiveKafka<>(this);
  }

  public void build() {
    inputs.forEach(System.out::println);
  }
}
