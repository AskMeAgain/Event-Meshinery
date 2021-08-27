package ask.me.again.core;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PassthroughProcessor<C extends Context> implements ReactiveProcessor<C> {

  private final String name;

  @Override
  public C process(C context) {
    System.out.println("Writing into Kafka Topic: " + name);
    return context;
  }
}
