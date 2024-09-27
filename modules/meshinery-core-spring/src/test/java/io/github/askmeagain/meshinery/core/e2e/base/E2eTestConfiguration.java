package io.github.askmeagain.meshinery.core.e2e.base;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.ITEMS;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.NUMBER_OF_TOPICS;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.RESULT_MAP_0;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.RESULT_MAP_1;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.RESULT_MAP_2;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.RESULT_MAP_3;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.TOPIC_PREFIX;

@Slf4j
@Configuration
public class E2eTestConfiguration {

  @Bean
  public MeshineryProcessor<TestContext, TestContext> testProcessor() {
    return new E2eTestProcessor();
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  @Bean
  public MeshineryTask<String, TestContext> Task100Loop(
      MeshineryProcessor<TestContext, TestContext> processor,
      MeshinerySourceConnector<String, TestContext> connector
  ) {
    var arr = IntStream.range(0, NUMBER_OF_TOPICS)
        .mapToObj(i -> TOPIC_PREFIX + i)
        .toArray(String[]::new);

    return MeshineryTask.<String, TestContext>builder()
        .connector(connector)
        .taskName("Task3Loop")
        .read(arr)
        .process(processor)
        .process(context -> {
          return context.withIndex(context.getIndex() + 1);
        })
        .process(context -> {
          if (context.getIndex() == 0) {
            RESULT_MAP_0.put(context.getId(), true);
          } else if (context.getIndex() == 1) {
            RESULT_MAP_1.put(context.getId(), true);
          } else if (context.getIndex() == 2) {
            RESULT_MAP_2.put(context.getId(), true);
          }
          return context;
        })
        .write(context -> {
          if (context.getIndex() >= NUMBER_OF_TOPICS) {
            RESULT_MAP_3.put(context.getId(), true);
            return "Finished";
          }
          return TOPIC_PREFIX + context.getIndex();
        })
        .build();
  }

  @Bean
  public MeshineryTask<String, TestContext> task1(MeshinerySourceConnector<String, TestContext> connector) {
    var inputSource = TestInputSource.builder()
        .todo(new TestContext(0))
        .iterations(ITEMS)
        .build();

    return MeshineryTask.<String, TestContext>builder()
        .outputSource(connector)
        .inputSource(inputSource)
        .taskName("InputSpawner")
        .read("Doesnt_matter")
        .process(x -> {
          RESULT_MAP_0.put(x.getId(), true);
          return x;
        })
        .write(TOPIC_PREFIX + "0")
        .build();
  }
}
