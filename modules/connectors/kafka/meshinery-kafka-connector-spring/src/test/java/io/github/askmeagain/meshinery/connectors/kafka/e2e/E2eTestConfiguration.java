package io.github.askmeagain.meshinery.connectors.kafka.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.kafka.MeshineryKafkaProperties;
import io.github.askmeagain.meshinery.connectors.kafka.sources.KafkaConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;

@Slf4j
@RequiredArgsConstructor
@TestConfiguration
public class E2eTestConfiguration {

  public static final int NUMBER_OF_TOPICS = 6;
  public static final int ITEMS = 20;
  public static final int THREADS = 20;
  public static final int SLEEP_IN_PROCESSOR = 100;
  public static final HashMap<Integer, HashSet<String>> RESULT_MAP = new HashMap<>();
  public static final String PREFIX = RandomStringUtils.random(10, true, false);

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public KafkaConnector<TestContext> kafkaConnector(
      ObjectMapper objectMapper,
      MeshineryKafkaProperties meshineryKafkaProperties
  ) {
    return new KafkaConnector<>(TestContext.class, objectMapper, meshineryKafkaProperties);
  }

  @Bean
  public MeshineryProcessor<TestContext, TestContext> testProcessor() {
    return new E2eTestProcessor();
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(THREADS);
  }

  @Bean
  public MeshineryTask<String, TestContext> Task100Loop(
      MeshineryProcessor<TestContext, TestContext> processor,
      KafkaConnector<TestContext> kafkaConnector,
      ExecutorService executorService
  ) {
    var arr = IntStream.range(0, NUMBER_OF_TOPICS)
        .mapToObj(i -> PREFIX + i)
        .toArray(String[]::new);

    return MeshineryTaskFactory.<String, TestContext>builder()
        .defaultOutputSource(kafkaConnector)
        .inputSource(kafkaConnector)
        .taskName("Task3Loop")
        .read(executorService, arr)
        .process(processor)
        .process(((context, e) -> CompletableFuture.completedFuture(context.withIndex(context.getIndex() + 1))))
        .write(context -> {
          if (context.getIndex() >= NUMBER_OF_TOPICS) {
            log.info("------ FINISHED %s ------".formatted(context.getId()));
            RESULT_MAP.get(NUMBER_OF_TOPICS).add(context.getId());
            return "Finished";
          }
          RESULT_MAP.get(context.getIndex()).add(context.getId());
          return PREFIX + context.getIndex();
        })
        .build();
  }

  @Bean
  public MeshineryTask<String, TestContext> task1(
      KafkaConnector<TestContext> kafkaConnector,
      ExecutorService executorService
  ) {
    var inputSource = TestInputSource.builder()
        .todo(TestContext.builder().build())
        .iterations(ITEMS)
        .build();

    return MeshineryTaskFactory.<String, TestContext>builder()
        .defaultOutputSource(kafkaConnector)
        .inputSource(inputSource)
        .taskName("InputSpawner")
        .read(executorService, "Doesnt matter")
        .write(PREFIX + "0")
        .build();
  }
}
