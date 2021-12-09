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
import java.util.List;
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

//@Disabled
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
      wait3Sec();
      return CompletableFuture.completedFuture(context);
    }

    @SneakyThrows
    private void wait3Sec() {
      //Thread.sleep(3000);
    }
  }

  @RequiredArgsConstructor
  @Configuration(proxyBeanMethods = false)
  public static class TestApplicationConfiguration {

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
      return Executors.newFixedThreadPool(4);
    }

    @Bean
    public MeshineryTask<String, TestContext> task1(
        MeshineryProcessor<TestContext, TestContext> processor,
        KafkaConnector<TestContext> kafkaConnector
    ) {
      var inputSource =
          new TestInputSource(List.of(TestContext.builder().build(), TestContext.builder().build()), 100, 0);

      return MeshineryTaskFactory.<String, TestContext>builder()
          .defaultOutputSource(kafkaConnector)
          .inputSource(inputSource)
          .taskName("InputSpawner")
          .read("Doesnt matter", Executors.newSingleThreadExecutor())
          .process(processor)
          .write("b")
          .build();
    }

    @Bean
    public MeshineryTask<String, TestContext> task2(
        MeshineryProcessor<TestContext, TestContext> processor,
        KafkaConnector<TestContext> kafkaConnector
    ) {
      return MeshineryTaskFactory.<String, TestContext>builder()
          .defaultOutputSource(kafkaConnector)
          .inputSource(kafkaConnector)
          .taskName("Cool task 2")
          .read("b", Executors.newSingleThreadExecutor())
          .process(processor)
          .process(((context, executor) -> {
            log.info("Received value");
            return CompletableFuture.completedFuture(context);
          }))
          .write("c")
          .build();
    }
  }
}
