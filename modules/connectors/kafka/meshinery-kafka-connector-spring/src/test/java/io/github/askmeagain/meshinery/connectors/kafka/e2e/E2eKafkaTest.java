package io.github.askmeagain.meshinery.connectors.kafka.e2e;

import io.github.askmeagain.meshinery.connectors.kafka.AbstractKafkaTest;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestBaseUtils;
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

import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestConfiguration.NUMBER_OF_TOPICS;
import static io.github.askmeagain.meshinery.core.e2e.base.E2eTestConfiguration.PREFIX;

@Slf4j
@SpringJUnitConfig(
    classes = {E2eTestApplication.class, KafkaTestConfiguration.class},
    initializers = ConfigDataApplicationContextInitializer.class
)
public class E2eKafkaTest extends AbstractKafkaTest {

  @Autowired
  ExecutorService executorService;

  @BeforeAll
  static void createTopics() {
    E2eTestBaseUtils.setupTest();

    var topics = IntStream.range(1, NUMBER_OF_TOPICS + 1)
        .mapToObj(i -> PREFIX + i)
        .toArray(String[]::new);

    createTopics(topics);
  }

  @Test
  @SneakyThrows
  void test() {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    executorService.awaitTermination(35_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    E2eTestBaseUtils.assertResultMap();
  }
}

