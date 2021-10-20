package ask.me.again.meshinery.connectors.kafka;

import ask.me.again.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import ask.me.again.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import ask.me.again.meshinery.connectors.kafka.sources.KafkaInputSource;
import ask.me.again.meshinery.connectors.kafka.sources.KafkaOutputSource;
import ask.me.again.meshinery.core.common.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaSourceTest extends AbstractKafkaTest {

  public static final String TOPIC = "Test";

  @Test
  void testInputOutput() {
    //Arrange ---------------------------------------------------------------------------------
    var kafkaProperties = getKafkaProperties();
    var consumerFactory = new KafkaConsumerFactory(kafkaProperties);
    var producerFactory = new KafkaProducerFactory(kafkaProperties);
    var inputSource = new KafkaInputSource<>(TestContext.class, new ObjectMapper(), consumerFactory);
    var outputSource = new KafkaOutputSource<>(producerFactory, new ObjectMapper());
    var input = TestContext.builder()
        .id("12")
        .build();

    //Act -------------------------------------------------------------------------------------
    outputSource.writeOutput(TOPIC, input);
    var result = inputSource.getInputs(TOPIC);

    //Assert ----------------------------------------------------------------------------------
    assertThat(result).contains(input);

  }

  @Value
  @Builder
  @Jacksonized
  @AllArgsConstructor
  private static class TestContext implements Context {
    String id;
  }

}
