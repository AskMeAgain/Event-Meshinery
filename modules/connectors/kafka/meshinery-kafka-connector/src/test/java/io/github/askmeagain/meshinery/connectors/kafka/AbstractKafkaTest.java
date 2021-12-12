package io.github.askmeagain.meshinery.connectors.kafka;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import org.testcontainers.utility.DockerImageName;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;

@Slf4j
public class AbstractKafkaTest {

  public static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
      .withEnv("KAFKA_TOPIC_METADATA_REFRESH_INTERVAL_MS", "10000")
      .waitingFor(Wait.forLogMessage(".*Kafka startTimeMs:.*", 1));

  @BeforeAll
  public static void setup() {
    kafkaContainer.start();
  }

  @DynamicPropertySource
  static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    registry.add("meshinery.connectors.kafka.bootstrap-servers", () -> kafkaContainer.getBootstrapServers());
  }

  public MeshineryKafkaProperties getKafkaProperties() {
    var kafkaProperties = new MeshineryKafkaProperties();
    kafkaProperties.setBootstrapServers(kafkaContainer.getBootstrapServers());
    kafkaProperties.setGroupId(RandomStringUtils.random(10, true, false));
    return kafkaProperties;
  }

  @SneakyThrows
  protected static void createTopics(String... topics) {
    var newTopics = Arrays.stream(topics)
        .map(topic -> new NewTopic(topic, 1, (short) 1))
        .collect(Collectors.toList());
    try (var admin = AdminClient.create(Map.of(BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers()))) {
      admin.createTopics(newTopics).all().get();
    }
  }
}
