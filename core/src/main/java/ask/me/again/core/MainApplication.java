package ask.me.again.core;

public class MainApplication {

  public static void main(String[] input) {
    new ReactiveKafka<TestContext>()
      .topic("topic-a")
      .run(new ProcessorA())
      .run(new ProcessorA())
      .topic("topic-b")
      .build();
  }
}