package ask.me.again.core;

public class MainApplication {

  public static void main(String[] input) {
    new ReactiveKafka<>(TestContext.class)
      .read("topic-a", "cool name")
      .process(new ProcessorA())
      .write("topic-b")
      .process(new ProcessorA())
      .write("topic-b")
      .process(new ProcessorA())
      .write("topic-b-FINISHED")
      .read("topic-a", "cool name2")
      .process(new ProcessorA())
      .write("topic-b")
      .process(new ProcessorA())
      .write("topic-b")
      .process(new ProcessorA())
      .write("topic-c-FINISHED")
      .build();
  }
}