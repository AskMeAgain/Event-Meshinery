package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.core.common.DataContext;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.source.CronInputSource;
import ask.me.again.meshinery.core.task.MeshineryTask;
import ask.me.again.meshinery.core.task.MeshineryTaskFactory;
import ask.me.again.meshinery.example.entities.ProcessorA;
import ask.me.again.meshinery.example.entities.ProcessorFinished;
import com.cronutils.model.CronType;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ExampleHeartbeatSplitJoinConfiguration {

  private final OutputSource<String, DataContext> outputSource;
  private final InputSource<String, DataContext> inputSource;

  private final ExecutorService executorService;

  private final ProcessorA processorA;
  private final ProcessorFinished processorFinished;

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, DataContext> task1() {
    return basicTask()
        .taskName("Start")
        .read("start", executorService)
        .process(processorA)
        .write("pre-split")
        .build();
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, DataContext> task2() {
    return basicTask()
        .taskName("Pre Split")
        .read("pre-split", executorService)
        .process(processorA)
        .write("left")
        .write("right")
        .build();
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, DataContext> task3() {
    return basicTask()
        .taskName("Left")
        .read("left", executorService)
        .process(processorA)
        .write("after-left")
        .build();
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, DataContext> task4() {
    return basicTask()
        .taskName("Right")
        .read("right", executorService)
        .process(processorA)
        .write("after-right")
        .build();
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, DataContext> join() {
    return basicTask()
        .taskName("Join")
        .joinOn(inputSource, "after-right", 5 * 60, (l, r) -> l)
        .read("after-left", executorService)
        .write("after-join")
        .build();
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, DataContext> afterJoinTask() {
    return basicTask()
        .taskName("After Join")
        .read("after-join", executorService)
        .process(processorFinished)
        .write("finished")
        .build();
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, DataContext> heartbeat() {
    var atomicInt = new AtomicInteger();
    var contextCronInputSource = new CronInputSource<>(
        "Cron heartbeat",
        CronType.SPRING,
        () -> () -> atomicInt.incrementAndGet() + ""
    );

    return MeshineryTaskFactory.<String, DataContext>builder()
        .inputSource(contextCronInputSource)
        .defaultOutputSource(outputSource)
        .taskName("Cron Heartbeat")
        .read("0/3 * 1 * * *", executorService)
        .write("start")
        .build();
  }

  private MeshineryTaskFactory<String, DataContext> basicTask() {
    return MeshineryTaskFactory.<String, DataContext>builder()
        .inputSource(inputSource)
        .defaultOutputSource(outputSource);
  }
}
