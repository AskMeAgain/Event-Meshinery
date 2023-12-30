package io.github.askmeagain.meshinery.connectors.pubsub.e2e;

import io.github.askmeagain.meshinery.connectors.pubsub.AbstractPubSubTestBase;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestBaseUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {E2eTestApplication.class, E2ePubSubTestConfiguration.class})
public class E2ePubSubTest extends AbstractPubSubTestBase {

  @Autowired
  ExecutorService executorService;

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
    //Act ------------------------------------------------------------------------------------
    var batchJobFinished = executorService.awaitTermination(15_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    E2eTestBaseUtils.assertResultMap();
  }
}