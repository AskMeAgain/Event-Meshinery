package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.example.TestContext;
import ask.me.again.meshinery.example.entities.ProcessorA;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleTaskConfiguration {

  private final OutputSource<String, TestContext> outputSource;
  private final InputSource<String, TestContext> inputSource;
  private final ExecutorService executorService;
  private final ProcessorA processorA;
  private final String prefix = "try-2-";

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public AtomicBoolean atomicBoolean() {
    return new AtomicBoolean(true);
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, TestContext> task1() {
    return MeshineryTask.<String, TestContext>builder()
        .defaultOutputSource(outputSource)
        .inputSource(inputSource)
        .taskName("Cool task 1")
        .read(prefix + "-a", executorService)
        .process(processorA)
        .write(prefix + "-b");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, TestContext> task2() {
    return MeshineryTask.<String, TestContext>builder()
        .defaultOutputSource(outputSource)
        .inputSource(inputSource)
        .taskName("Cool task 2")
        .read(prefix + "-b", executorService)
        .process(processorA)
        .write(prefix + "-c");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, TestContext> task3() {
    return MeshineryTask.<String, TestContext>builder()
        .defaultOutputSource(outputSource)
        .inputSource(inputSource)
        .taskName("Cool task 3")
        .read(prefix + "-c", executorService)
        .process(processorA)
        .write(prefix + "-d");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, TestContext> task4() {
    return MeshineryTask.<String, TestContext>builder()
        .defaultOutputSource(outputSource)
        .inputSource(inputSource)
        .taskName("Cool task 4")
        .read(prefix + "-d", executorService)
        .process(processorA)
        .write(prefix + "-e");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, TestContext> task5() {
    return MeshineryTask.<String, TestContext>builder()
        .defaultOutputSource(outputSource)
        .inputSource(inputSource)
        .taskName("Cool task 5")
        .read(prefix + "-e", executorService)
        .process(processorA)
        .write(prefix + "-i");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, TestContext> task6() {
    return MeshineryTask.<String, TestContext>builder()
        .defaultOutputSource(outputSource)
        .inputSource(inputSource)
        .taskName("Cool task 6")
        .read(prefix + "-b", executorService)
        .process(processorA)
        .write(prefix + "-g");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, TestContext> task7() {
    return MeshineryTask.<String, TestContext>builder()
        .defaultOutputSource(outputSource)
        .inputSource(inputSource)
        .taskName("Cool task 7")
        .read(prefix + "-g", executorService)
        .process(processorA)
        .write(prefix + "-h");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, TestContext> task8() {
    return MeshineryTask.<String, TestContext>builder()
        .defaultOutputSource(outputSource)
        .inputSource(inputSource)
        .taskName("Cool task 8")
        .read(prefix + "-d", executorService)
        .process(processorA)
        .write(prefix + "-i");
  }
}
