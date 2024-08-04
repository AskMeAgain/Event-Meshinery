package io.github.askmeagain.meshinery.connectors.pubsub.e2e;

import io.github.askmeagain.meshinery.connectors.pubsub.AbstractSpringPubSubTestBase;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestBaseUtils;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@SpringBootTest(classes = {E2eTestApplication.class, E2ePubSubTestConfiguration.class})
@TestPropertySource(properties = {
    "meshinery.core.batch-job=true",
    "meshinery.core.shutdown-on-finished=false",
    "meshinery.core.grace-period-milliseconds=5000",
    "meshinery.core.backpressure-limit=150",
    "meshinery.core.start-immediately=false"
})
public class E2ePubSubTest extends AbstractSpringPubSubTestBase {

  @Autowired
  ExecutorService executorService;
  @Autowired RoundRobinScheduler roundRobinScheduler;

  @BeforeAll
  static void setupTest() {
    E2eTestBaseUtils.setupTest();

    for (var i = 0; i < E2eTestApplication.NUMBER_OF_TOPICS; i++) {
      var topicName = E2eTestApplication.TOPIC_PREFIX + i;
      createTopic(topicName);
      createSubscription(topicName, topicName + "_subscription");
    }
    createTopic("Finished");
    createTopic("Finished_subscription");
  }

  @Test
  @SneakyThrows
  void testE2ePubSub() {
    //Arrange --------------------------------------------------------------------------------
    roundRobinScheduler.start();

    //Act ------------------------------------------------------------------------------------
    var batchJobFinished = executorService.awaitTermination(160_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    E2eTestBaseUtils.assertResultMap();
  }
}