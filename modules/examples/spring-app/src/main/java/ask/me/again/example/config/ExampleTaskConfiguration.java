package ask.me.again.example.config;

import ask.me.again.core.common.MeshineryTask;
import ask.me.again.core.common.OutputSource;
import ask.me.again.example.TestContext;
import ask.me.again.example.entities.ProcessorA;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@Configuration
@RequiredArgsConstructor
public class ExampleTaskConfiguration {

  private final OutputSource<String, TestContext> outputSource;
  private final ExecutorService executorService;
  private final ProcessorA processorA;

  @Bean
  public AtomicBoolean atomicBoolean() {
    return new AtomicBoolean(true);
  }

  @Bean
  public MeshineryTask<String, TestContext> task1() {
    return MeshineryTask.<String, TestContext>builder()
      .outputSource(outputSource)
      .taskName("Cool task 1")
      .read("topic-x", executorService)
      .process(processorA)
      .write("topic-y")
      .process(processorA)
      .write("topic-z")
      .process(processorA)
      .write("topic-w-FINISHED")
      .build();
  }

  @Bean
  public MeshineryTask<String, TestContext> task2() {
    return MeshineryTask.<String, TestContext>builder()
      .outputSource(outputSource)
      .taskName("Cool task 2")
      .read("topic-a", executorService)
      .process(processorA)
      .write("topic-b")
      .process(processorA)
      .write("topic-c")
      .process(processorA)
      .write("topic-d-FINISHED")
      .build();
  }
}
