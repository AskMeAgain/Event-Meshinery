package io.github.askmeagain.meshinery.connectors.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Configuration
@EnableConfigurationProperties
public class MeshineryKafkaConfiguration {

  @Bean
  public DynamicKafkaConnectorRegistration dynamicKafkaConnectorRegistration(
      ApplicationContext applicationContext,
      ObjectMapper objectMapper,
      ObjectProvider<MeshineryKafkaProperties> meshineryKafkaProperties
  ) {
    return new DynamicKafkaConnectorRegistration(applicationContext, objectMapper, meshineryKafkaProperties);
  }

  @Bean
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public KafkaConsumerFactory kafkaConsumerFactory(MeshineryKafkaProperties meshineryKafkaProperties) {
    return new KafkaConsumerFactory(meshineryKafkaProperties);
  }

  @Bean
  public KafkaProducerFactory kafkaProducerFactory(MeshineryKafkaProperties meshineryKafkaProperties) {
    return new KafkaProducerFactory(meshineryKafkaProperties);
  }

  @Bean
  @Validated
  @ConfigurationProperties("meshinery.connectors.kafka")
  public MeshineryKafkaProperties kafkaProperties() {
    return new MeshineryKafkaProperties();
  }

}
