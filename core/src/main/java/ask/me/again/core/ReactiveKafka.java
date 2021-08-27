package ask.me.again.core;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ReactiveKafka<C extends Context> {

  @Getter
  private final List<Operation<C>> inputs;

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

  public ReactiveKafka<C> topic(String input, boolean passthrough) {
    inputs.add(Operation.<C>builder()
      .topicName(input)
      .passthrough(passthrough)
      .build());
    return new ReactiveKafka<>(this);
  }

  public ReactiveKafka<C> run(ReactiveProcessor<C> processor) {
    inputs.add(Operation.<C>builder()
      .processor(processor)
      .build());
    return new ReactiveKafka<>(this);
  }


  public List<ReactiveProcessor<C>> build() {

    var list = new ArrayList<ReactiveProcessor<C>>();

    for (int i = 0; i < inputs.size(); i++) {

      var operation = inputs.get(i);
      if (operation.isPassthrough()) {
        list.add(new PassthroughProcessor<C>(operation.getTopicName()));
      } else if (operation.getProcessor() != null) {
        list.add(operation.getProcessor());
      }
    }

    return list;
  }
}
