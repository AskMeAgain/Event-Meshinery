package io.github.askmeagain.meshinery.connectors.kafka.e2e;

import io.github.askmeagain.meshinery.connectors.kafka.AbstractSpringKafkaTestBase;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestBaseUtils;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.NUMBER_OF_TOPICS;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication.TOPIC_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {E2eTestApplication.class, E2eKafkaTestConfiguration.class})
@TestPropertySource(properties = {
    "meshinery.core.batch-job=true",
    "meshinery.core.shutdown-on-finished=false",
    "meshinery.core.grace-period-milliseconds=15000",
    "meshinery.core.backpressure-limit=150",
    "meshinery.core.start-immediately=false"
})
class E2eKafkaTest extends AbstractSpringKafkaTestBase {

  @Autowired ExecutorService executorService;
  @Autowired RoundRobinScheduler roundRobinScheduler;

  @BeforeAll
  static void createTopics() {
    E2eTestBaseUtils.setupTest();

    var topics = IntStream.range(1, NUMBER_OF_TOPICS + 1)
        .mapToObj(i -> TOPIC_PREFIX + i)
        .toArray(String[]::new);

    createTopics(topics);
  }

  @Test
  @SneakyThrows
  void test() {
    //Arrange --------------------------------------------------------------------------------
    roundRobinScheduler.start();

    //Act ------------------------------------------------------------------------------------
    var batchJobIsFinished = executorService.awaitTermination(160_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobIsFinished).isTrue();
    E2eTestBaseUtils.assertResultMap();
  }
}

