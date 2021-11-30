package io.github.askmeagain.meshinery.connectors.kafka;

import io.github.askmeagain.meshinery.connectors.kafka.sources.KafkaConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaSourceTest extends AbstractKafkaTest {

  public static final String TOPIC = "Test";

  @Test
  void testInputOutput() {
    //Arrange ---------------------------------------------------------------------------------
    var objectMapper = new ObjectMapper();
    var kafkaProperties = getKafkaProperties();
    var connector = new KafkaConnector<>("kafka-default-source", TestContext.class, objectMapper, kafkaProperties);
    var input = new TestContext(12);

    //Act -------------------------------------------------------------------------------------
    connector.writeOutput(TOPIC, input);
    var result = connector.getInputs(TOPIC);

    //Assert ----------------------------------------------------------------------------------
    assertThat(result).contains(input);
  }
}
