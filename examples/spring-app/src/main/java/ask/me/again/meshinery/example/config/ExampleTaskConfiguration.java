package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.example.TestContext;
import ask.me.again.meshinery.example.entities.ProcessorA;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@RequiredArgsConstructor
public class ExampleTaskConfiguration {

  private final OutputSource<String, TestContext> outputSource;
  private final InputSource<String, TestContext> inputSource;
  private final ExecutorService executorService;
  private final ProcessorA processorA;

  @Bean
  public AtomicBoolean atomicBoolean() {
    return new AtomicBoolean(true);
  }

  @Bean
  public MeshineryTask<String, TestContext, TestContext> task1() {
    return new MeshineryTask<String, TestContext, TestContext>()
        .inputSource(inputSource)
        .outputSource(outputSource)
        .taskName("Cool task 1")
        .read("topic-x", executorService)
        .process(processorA)
        .write("topic-y")
        .process(processorA)
        .write("topic-z")
        .process(processorA)
        .write("topic-w-FINISHED");
  }

  @Bean
  public MeshineryTask<String, TestContext, TestContext> task2() {
    return new MeshineryTask<String, TestContext, TestContext>()
        .inputSource(inputSource)
        .outputSource(outputSource)
        .taskName("Cool task 2")
        .read("topic-a", executorService)
        .process(processorA)
        .write("topic-b")
        .process(processorA)
        .write("topic-c")
        .process(processorA)
        .write("topic-d-FINISHED");
  }

  @Bean
  public MeshineryTask<String, TestContext, TestContext> task3() {
    return new MeshineryTask<String, TestContext, TestContext>()
        .inputSource(inputSource)
        .outputSource(outputSource)
        .taskName("Endpoint 1")
        .read("topic-a", executorService)
        .process(processorA)
        .write("topic-d-FINISHED");
  }
}
