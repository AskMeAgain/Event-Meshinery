package io.github.askmeagain.meshinery.connectors.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.Collections;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaSourceTest extends AbstractKafkaTest {

  private static final String TOPIC = "TOPIC";

  @BeforeAll
  static void creatingTopics() {
    createTopics(TOPIC);
  }

  @Test
  @SneakyThrows
  void testInputOutput() {
    //Arrange ---------------------------------------------------------------------------------
    var objectMapper = new ObjectMapper();
    var kafkaProperties = getKafkaProperties();
    var connector = new KafkaConnector<>(TestContext.class, objectMapper, kafkaProperties);
    var input = new TestContext(12);

    //Act -------------------------------------------------------------------------------------
    connector.writeOutput(TOPIC, input, new TaskData());

    List<TestContext> result = Collections.emptyList();
    //this is to wait for metadata updates
    for (int i = 0; i < 30; i++) {
      result = connector.getInputs(List.of(TOPIC));
      if (!result.isEmpty()) {
        break;
      }
      Thread.sleep(1000);
    }

    //Assert ----------------------------------------------------------------------------------
    assertThat(result).contains(input);
    connector.close();
  }
}
