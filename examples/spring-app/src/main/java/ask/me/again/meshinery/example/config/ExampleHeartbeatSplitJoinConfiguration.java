package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.MeshineryTask;
import ask.me.again.meshinery.core.common.OutputSource;
import ask.me.again.meshinery.core.source.CronInputSource;
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

  private final OutputSource<String, Context> outputSource;
  private final InputSource<String, Context> inputSource;

  private final ExecutorService executorService;

  private final ProcessorA processorA;
  private final ProcessorFinished processorFinished;

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
        .taskName("Pre Split")
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

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, Context> join() {
    return basicTask()
        .taskName("Join")
        .joinOn(inputSource, "after-right", (l, r) -> l)
        .read("after-left", executorService)
        .write("after-join");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, Context> afterJoinTask() {
    return basicTask()
        .taskName("After Join")
        .read("after-join", executorService)
        .process(processorFinished)
        .write("finished");
  }

  @Bean
  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public MeshineryTask<String, Context> heartbeat() {
    var atomicInt = new AtomicInteger();
    var contextCronInputSource = new CronInputSource<>(
        "Cron heartbeat",
        CronType.SPRING,
        () -> createNewContext(atomicInt.incrementAndGet())
    );

    return MeshineryTask.<String, Context>builder()
        .inputSource(contextCronInputSource)
        .defaultOutputSource(outputSource)
        .taskName("Cron Heartbeat")
        .read("0/3 * 1 * * *", executorService)
        .write("start");
  }

  private Context createNewContext(int index) {
    return () -> index + "";
  }

  private MeshineryTask<String, Context> basicTask() {
    return MeshineryTask.<String, Context>builder()
        .inputSource(inputSource)
        .defaultOutputSource(outputSource);
  }
}
