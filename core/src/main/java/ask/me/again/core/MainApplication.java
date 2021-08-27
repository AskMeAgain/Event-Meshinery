package ask.me.again.core;

public class MainApplication {

  public static void main(String[] input) {
    var result = new ReactiveKafka<TestContext>()
      .topic("topic-a",false)
      .run(new ProcessorA())
      .run(new ProcessorA())
      .topic("topic-b",true)
      .run(new ProcessorA())
      .run(new ProcessorA())
      .build();

    var orig = TestContext.builder().build();

    for(var processor: result){
      orig = processor.process(orig);
    }
  }
}