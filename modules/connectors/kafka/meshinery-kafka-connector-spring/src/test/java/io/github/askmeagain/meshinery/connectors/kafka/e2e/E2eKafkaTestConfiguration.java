package io.github.askmeagain.meshinery.connectors.kafka.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.kafka.EnableMeshineryKafkaConnector;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import io.github.askmeagain.meshinery.connectors.kafka.sources.KafkaConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableMeshineryKafkaConnector
public class E2eKafkaTestConfiguration {

  @Bean
  public KafkaConnector<TestContext> kafkaConnector(
      ObjectMapper objectMapper,
      KafkaProducerFactory producerFactory,
      KafkaConsumerFactory consumerFactory
  ) {
    return new KafkaConnector<>("name", TestContext.class, objectMapper, consumerFactory, producerFactory);
  }

}
