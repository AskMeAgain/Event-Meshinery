package io.github.askmeagain.meshinery.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.kafka.EnableMeshineryKafkaConnector;
import io.github.askmeagain.meshinery.connectors.kafka.MeshineryKafkaProperties;
import io.github.askmeagain.meshinery.connectors.kafka.sources.KafkaConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import io.github.askmeagain.meshinery.monitoring.EnableMeshineryMonitoring;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@EnableMeshinery
@EnableMeshineryMonitoring
@EnableMeshineryKafkaConnector
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
      return CompletableFuture.supplyAsync(() -> {
        wait1Sec();
        return context;
      }, executor);
    }

    @SneakyThrows
    private void wait1Sec() {
      Thread.sleep(3000);
    }
  }

  @RequiredArgsConstructor
  @Configuration(proxyBeanMethods = false)
  public static class TestApplicationConfiguration {

    private static final String PREFIX = "stress-test-1-";

    @Bean
    public KafkaConnector<TestContext> kafkaConnector(
        ObjectMapper objectMapper, MeshineryKafkaProperties meshineryKafkaProperties
    ) {
      return new KafkaConnector<>(TestContext.class, objectMapper, meshineryKafkaProperties);
    }

    @Bean
    public MeshineryProcessor<TestContext, TestContext> testProcessor() {
      return new TestProcessor();
    }

    @Bean
    public ExecutorService executorService() {
      return Executors.newFixedThreadPool(20);
    }

    @Bean
    public MeshineryTask<String, TestContext> Task100Loop(
        MeshineryProcessor<TestContext, TestContext> processor,
        KafkaConnector<TestContext> kafkaConnector,
        ExecutorService executorService
    ) {
      return MeshineryTaskFactory.<String, TestContext>builder()
          .defaultOutputSource(kafkaConnector)
          .inputSource(kafkaConnector)
          .taskName("Task100Loop")
          .read(PREFIX + "b", executorService)
          .process(processor)
          .process(((context, executor) -> {
            log.info("------ %s ------".formatted(context.getIndex()));
            return CompletableFuture.completedFuture(context.withIndex(context.getIndex() + 1));
          }))
          .write(c -> {
            if (c.getIndex() > 10) {
              return "Finished";
            }
            return PREFIX + c.getIndex();
          })
          .build();
    }

    @Bean
    public MeshineryTask<String, TestContext> task1(
        MeshineryProcessor<TestContext, TestContext> processor,
        KafkaConnector<TestContext> kafkaConnector,
        ExecutorService executorService
    ) {
      var inputSource = TestInputSource.builder()
          .todo(TestContext.builder().build())
          .iterations(20)
          .build();

      return MeshineryTaskFactory.<String, TestContext>builder()
          .defaultOutputSource(kafkaConnector)
          .inputSource(inputSource)
          .taskName("InputSpawner")
          .read("Doesnt matter", executorService)
          .process(((context, executor) -> {
            log.info("------ First Processor START------");
            return CompletableFuture.completedFuture(context);
          }))
          .process(processor)
          .process(((context, executor) -> {
            log.info("------ First Processor END ------");
            return CompletableFuture.completedFuture(context);
          }))
          .write(PREFIX + "b")
          .build();
    }

    @Bean
    public MeshineryTask<String, TestContext> task2(
        MeshineryProcessor<TestContext, TestContext> processor,
        KafkaConnector<TestContext> kafkaConnector,
        ExecutorService executorService
    ) {
      return MeshineryTaskFactory.<String, TestContext>builder()
          .defaultOutputSource(kafkaConnector)
          .inputSource(kafkaConnector)
          .taskName("Cool task 2")
          .read(PREFIX + "b", executorService)
          .process(((context, executor) -> {
            log.info("------ Second Processor START ------");
            return CompletableFuture.completedFuture(context);
          }))
          .process(processor)
          .process(((context, executor) -> {
            log.info("------ Second Processor END ------");
            return CompletableFuture.completedFuture(context);
          }))
          .write(PREFIX + "c")
          .build();
    }

    @Bean
    public MeshineryTask<String, TestContext> task3(
        MeshineryProcessor<TestContext, TestContext> processor,
        KafkaConnector<TestContext> kafkaConnector,
        ExecutorService executorService
    ) {
      return MeshineryTaskFactory.<String, TestContext>builder()
          .defaultOutputSource(kafkaConnector)
          .inputSource(kafkaConnector)
          .taskName("Cool task 3")
          .read(PREFIX + "c", executorService)
          .process(((context, executor) -> {
            log.info("------ THIRD PROCESSOR START ------");
            return CompletableFuture.completedFuture(context);
          }))
          .process(processor)
          .process(((context, executor) -> {
            log.info("------ THIRD PROCESSOR END ------");
            return CompletableFuture.completedFuture(context);
          }))
          .write(PREFIX + "finished")
          .build();
    }
  }
}
