package io.github.askmeagain.meshinery.core;

import com.cronutils.model.CronType;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.source.CronInputSource;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.monitoring.EnableMeshineryMonitoring;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@EnableMeshinery
@EnableMeshineryMonitoring
@SpringBootApplication
@Import({TestApplication.TestApplicationConfiguration.class})
public class TestApplication {
  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

  public static class TestProcessor implements MeshineryProcessor<TestContext, TestContext> {
    @Override
    public CompletableFuture<TestContext> processAsync(
        TestContext context, Executor executor
    ) {
      wait3Sec();
      return CompletableFuture.completedFuture(context);
    }

    @SneakyThrows
    private void wait3Sec() {
      Thread.sleep(3000);
    }
  }

  @RequiredArgsConstructor
  @Configuration(proxyBeanMethods = false)
  public static class TestApplicationConfiguration {

    @Bean
    public MemoryConnector<String, TestContext> memoryConnector() {
      return new MemoryConnector<>();
    }

    @Bean
    public MeshineryProcessor<TestContext, TestContext> testProcessor() {
      return new TestProcessor();
    }


    @Bean
    public ExecutorService executorService() {
      return Executors.newFixedThreadPool(4);
    }

    @Bean
    public MeshineryTask<String, TestContext> task1(
        MeshineryProcessor<TestContext, TestContext> processor,
        MemoryConnector<String, TestContext> memoryConnector
    ) {
      var cronInput = new CronInputSource<>("cron input", CronType.SPRING, () -> TestContext.builder()
          .id((int) ((Math.random() * 500000)) + "")
          .build());

      return MeshineryTaskFactory.<String, TestContext>builder()
          .defaultOutputSource(memoryConnector)
          .inputSource(cronInput)
          .taskName("Cool task 1")
          .read("0/1 * * * * *", Executors.newSingleThreadExecutor())
          .process(processor)
          .write("b")
          .build();
    }

    @Bean
    public MeshineryTask<String, TestContext> task2(
        MeshineryProcessor<TestContext, TestContext> processor,
        MemoryConnector<String, TestContext> memoryConnector
    ) {
      return MeshineryTaskFactory.<String, TestContext>builder()
          .defaultOutputSource(memoryConnector)
          .inputSource(memoryConnector)
          .taskName("Cool task 2")
          .read("b", Executors.newSingleThreadExecutor())
          .process(processor)
          .write("c")
          .build();
    }
  }
}
