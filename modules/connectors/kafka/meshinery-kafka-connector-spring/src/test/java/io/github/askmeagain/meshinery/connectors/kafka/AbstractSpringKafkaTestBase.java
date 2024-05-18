package io.github.askmeagain.meshinery.connectors.kafka;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class AbstractSpringKafkaTestBase extends AbstractKafkaTest {

  @DynamicPropertySource
  static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    registry.add("meshinery.connectors.kafka.bootstrap-servers", () -> kafkaContainer.getBootstrapServers());
  }
}
