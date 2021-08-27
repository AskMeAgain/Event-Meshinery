package ask.me.again.core;

public class MainApplication {

  public static void main(String[] input) {
    var result = new ReactiveKafka<TestContext>()
      .read("topic-a")
      .run(new ProcessorA())
      .run(new ProcessorA())
      .write("topic-b")
      .write("topic-b")
      .run(new ProcessorA())
      .run(new ProcessorA())
      .build();

    var orig = TestContext.builder().build();

    for(var processor: result){
      orig = processor.process(orig);
    }
  }
}