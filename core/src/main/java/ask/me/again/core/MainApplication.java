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
      .write("topic-b-FINISHED")
      .read("topic-a", "cool name2")
      .run(new ProcessorA())
      .write("topic-b")
      .run(new ProcessorA())
      .write("topic-b")
      .run(new ProcessorA())
      .write("topic-c-FINISHED")
      .build();
  }
}