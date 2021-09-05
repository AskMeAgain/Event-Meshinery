package ask.me.again.meshinery.connectors.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
