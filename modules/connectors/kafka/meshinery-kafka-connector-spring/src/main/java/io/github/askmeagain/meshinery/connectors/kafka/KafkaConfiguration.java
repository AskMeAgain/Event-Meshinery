package io.github.askmeagain.meshinery.connectors.kafka;

import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Configuration
@EnableConfigurationProperties
public class KafkaConfiguration {

  @Bean
  public KafkaConsumerFactory kafkaConsumerFactory(KafkaProperties kafkaProperties) {
    return new KafkaConsumerFactory(kafkaProperties);
  }

  @Bean
  public KafkaProducerFactory kafkaProducerFactory(KafkaProperties kafkaProperties) {
    return new KafkaProducerFactory(kafkaProperties);
  }

  @Bean
  @ConfigurationProperties("meshinery.connectors.kafka")
  public KafkaProperties kafkaProperties() {
    return new KafkaProperties();
  }
}