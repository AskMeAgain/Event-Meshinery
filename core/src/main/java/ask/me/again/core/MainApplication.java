package ask.me.again.core;

public class MainApplication {

  public static void main(String[] input) {
    new ReactiveKafka<>(TestContext.class)
      .read("topic-a", "cool name")
      .run(new ProcessorA())
      .write("topic-b")
      .run(new ProcessorA())
      .write("topic-b")
      .run(new ProcessorA())
      .write("topic-b")
      .build();
  }
}