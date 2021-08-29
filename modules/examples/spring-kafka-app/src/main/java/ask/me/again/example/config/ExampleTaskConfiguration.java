package ask.me.again.example.config;

import ask.me.again.core.common.ReactiveTask;
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
  public ReactiveTask<String, TestContext> task1() {
    return ReactiveTask.<String, TestContext>builder()
      .outputSource(outputSource)
      .taskName("Cool task 1")
      .read("topic-b", executorService)
      .process(processorA)
      .write("test-1")
      .build();
  }

  @Bean
  public ReactiveTask<String, TestContext> task2() {
    return ReactiveTask.<String, TestContext>builder()
      .outputSource(outputSource)
      .taskName("Cool task 2")
      .read("test-1", executorService)
      .process(processorA)
      .write("test-if-you-see-this-its-working")
      .build();
  }
}
