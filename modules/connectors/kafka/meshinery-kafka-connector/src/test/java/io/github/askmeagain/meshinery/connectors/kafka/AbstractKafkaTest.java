package io.github.askmeagain.meshinery.connectors.kafka;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class AbstractKafkaTest {

  public static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))
      .waitingFor(Wait.forLogMessage(".*Kafka startTimeMs:.*", 1));

  @BeforeAll
  public static void setup() {
    kafkaContainer.start();
  }

  public MeshineryKafkaProperties getKafkaProperties() {
    var kafkaProperties = new MeshineryKafkaProperties();
    kafkaProperties.setBootstrapServers(kafkaContainer.getBootstrapServers());
    kafkaProperties.setGroupId("Test");
    return kafkaProperties;
  }
}
