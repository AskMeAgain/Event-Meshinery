package io.github.askmeagain.meshinery.core.e2e.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.ITEMS;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.NUMBER_OF_TOPICS;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.RESULT_MAP;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.THREADS;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.TOPIC_PREFIX;

@Slf4j
@TestConfiguration
public class E2eTestConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
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
      MeshineryConnector<String, TestContext> connector,
      ExecutorService executorService
  ) {
    var arr = IntStream.range(0, NUMBER_OF_TOPICS)
        .mapToObj(i -> TOPIC_PREFIX + i)
        .toArray(String[]::new);

    return MeshineryTaskFactory.<String, TestContext>builder()
        .outputSource(connector)
        .inputSource(connector)
        .taskName("Task3Loop")
        .read(executorService, arr)
        .process(processor)
        .process(((context, e) -> CompletableFuture.completedFuture(context.withIndex(context.getIndex() + 1))))
        .process(((context, e) -> {
          RESULT_MAP.get(context.getIndex()).add(context.getId());
          if (context.getIndex() >= NUMBER_OF_TOPICS) {
            log.warn("------ FINISHED %s ------".formatted(context.getId()));
          }
          return CompletableFuture.completedFuture(context);
        }))
        .write(context -> {
          if (context.getIndex() >= NUMBER_OF_TOPICS) {
            return "Finished";
          }
          return TOPIC_PREFIX + context.getIndex();
        })
        .build();
  }

  @Bean
  public MeshineryTask<String, TestContext> task1(
      MeshineryConnector<String, TestContext> connector,
      ExecutorService executorService
  ) {
    var inputSource = TestInputSource.builder()
        .todo(TestContext.builder().build())
        .iterations(ITEMS)
        .build();

    return MeshineryTaskFactory.<String, TestContext>builder()
        .outputSource(connector)
        .inputSource(inputSource)
        .taskName("InputSpawner")
        .read(executorService, "Doesnt matter")
        .write(TOPIC_PREFIX + "0")
        .build();
  }
}
