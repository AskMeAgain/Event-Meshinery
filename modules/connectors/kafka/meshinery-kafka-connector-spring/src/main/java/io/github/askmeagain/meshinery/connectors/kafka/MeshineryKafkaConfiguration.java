package io.github.askmeagain.meshinery.connectors.kafka;

import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Configuration
public class MeshineryKafkaConfiguration {

  @Bean
  public KafkaConsumerFactory kafkaConsumerFactory(MeshineryKafkaProperties meshineryKafkaProperties) {
    return new KafkaConsumerFactory(meshineryKafkaProperties);
  }

  @Bean
  public KafkaProducerFactory kafkaProducerFactory(MeshineryKafkaProperties meshineryKafkaProperties) {
    return new KafkaProducerFactory(meshineryKafkaProperties);
  }

  @Bean
  @ConfigurationProperties("meshinery.connectors.kafka")
  public MeshineryKafkaProperties kafkaProperties() {
    return new MeshineryKafkaProperties();
  }
}
