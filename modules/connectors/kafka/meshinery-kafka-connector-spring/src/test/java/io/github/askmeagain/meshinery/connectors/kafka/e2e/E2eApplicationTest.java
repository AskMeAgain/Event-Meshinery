package io.github.askmeagain.meshinery.connectors.kafka.e2e;

import io.github.askmeagain.meshinery.connectors.kafka.AbstractKafkaTest;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static io.github.askmeagain.meshinery.connectors.kafka.e2e.E2eTestConfiguration.ITEMS;
import static io.github.askmeagain.meshinery.connectors.kafka.e2e.E2eTestConfiguration.NUMBER_OF_TOPICS;
import static io.github.askmeagain.meshinery.connectors.kafka.e2e.E2eTestConfiguration.PREFIX;
import static io.github.askmeagain.meshinery.connectors.kafka.e2e.E2eTestConfiguration.RESULT_MAP;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringJUnitConfig(
    classes = E2eTestApplication.class,
    initializers = ConfigDataApplicationContextInitializer.class
)
public class E2eApplicationTest extends AbstractKafkaTest {

  @Autowired
  RoundRobinScheduler roundRobinScheduler;

  @BeforeAll
  static void createTopics() {
    IntStream.range(1, NUMBER_OF_TOPICS + 1)
        .forEach(i -> RESULT_MAP.put(i, new ArrayList<>()));
    var topics = IntStream.range(1, NUMBER_OF_TOPICS + 1)
        .mapToObj(i -> PREFIX + i)
        .toArray(String[]::new);

    createTopics(topics);
  }

  @Autowired
  ExecutorService executorService;

  @Test
  @SneakyThrows
  void test() {
    //Arrange --------------------------------------------------------------------------------
    var resultSet = IntStream.range(1, ITEMS + 1)
        .mapToObj(i -> "" + i)
        .toArray(String[]::new);

    //Act ------------------------------------------------------------------------------------
    executorService.awaitTermination(35_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(RESULT_MAP)
        .extracting(Map::values)
        .satisfies(x -> assertThat(x)
            .allSatisfy(y -> assertThat(y).containsExactlyInAnyOrder(resultSet)));
  }
}
