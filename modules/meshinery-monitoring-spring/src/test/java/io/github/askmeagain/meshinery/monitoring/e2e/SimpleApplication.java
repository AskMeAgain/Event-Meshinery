package io.github.askmeagain.meshinery.monitoring.e2e;

import com.cronutils.model.CronType;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import io.github.askmeagain.meshinery.core.source.CronInputSource;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.monitoring.common.EnableMeshineryMonitoring;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableMeshinery
@EnableMeshineryMonitoring
public class SimpleApplication {

  public static void main(String... args) {
    SpringApplication.run(SimpleApplication.class);
  }

  @Bean
  MeshineryTask<String, TestContext> task1(ExecutorService executorService) {
    var cronSource = new CronInputSource<>(CronType.SPRING, () -> new TestContext(0));
    var memoryConnector = new MemoryConnector<String, TestContext>();
    var atomicInt = new AtomicInteger();
    return MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(cronSource)
        .taskName("task1")
        .outputSource(memoryConnector)
        .read(executorService, "0/1 * * * * *")
        .process((c, e) -> CompletableFuture.completedFuture(c.withId("" + atomicInt.getAndIncrement())))
        .write("Next-Step")
        .build();
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(2);
  }

  @Bean
  MeshineryTask<String, TestContext> task2(ExecutorService executorService) {
    var memoryConnector = new MemoryConnector<String, TestContext>();

    return MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(memoryConnector)
        .taskName("task2")
        .outputSource(memoryConnector)
        .read(executorService, "Next-Step")
        .write("Next-Step-2")
        .build();
  }

  @Bean
  MeshineryTask<String, TestContext> task3(ExecutorService executorService) {
    var memoryConnector = new MemoryConnector<String, TestContext>();

    return MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(memoryConnector)
        .taskName("task3")
        .outputSource(memoryConnector)
        .read(executorService, "Next-Step-2")
        .write("End-Step")
        .build();
  }
}
