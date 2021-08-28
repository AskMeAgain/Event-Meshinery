package ask.me.again.core.example;

import ask.me.again.core.builder.PipelineBuilder;
import ask.me.again.core.processors.ProcessorA;

public class MainApplication {

  public static void main(String[] input) {

    var processor = new ProcessorA();

    new PipelineBuilder<>(TestContext.class)
      .usingExecutor()
      //task 1
      .read("topic-a", "cool name")
      .process(processor)
      .write("topic-b")
      .process(processor)
      .write("topic-b")
      .process(processor)
      .write("topic-b-FINISHED")
      //task 2
      .read("topic-a", "cool name2")
      .process(processor)
      .write("topic-b")
      .process(processor)
      .write("topic-b")
      .process(processor)
      .write("topic-c-FINISHED")
      .build();
  }
}