package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.example.entities.ProcessorA;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleTaskConfiguration {

  private final OutputSource<String, Context> outputSource;
  private final InputSource<String, Context> inputSource;

  private final ExecutorService executorService;

  private final ProcessorA processorA;

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, Context> task1() {
    return basicTask()
        .taskName("Start")
        .read("start", executorService)
        .process(processorA)
        .write("pre-split");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, Context> task2() {
    return basicTask()
        .taskName("Pre Split Task")
        .read("pre-split", executorService)
        .process(processorA)
        .write("left")
        .write("right");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, Context> task3() {
    return basicTask()
        .taskName("Left")
        .read("left", executorService)
        .process(processorA)
        .write("after-left");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, Context> task4() {
    return basicTask()
        .taskName("Right")
        .read("right", executorService)
        .process(processorA)
        .write("after-right");
  }

  private MeshineryTask<String, Context> basicTask() {
    return MeshineryTask.<String, Context>builder()
        .inputSource(inputSource)
        .defaultOutputSource(outputSource);
  }
}
