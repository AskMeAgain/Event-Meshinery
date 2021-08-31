package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.connectors.kafka.KafkaConsumerFactory;
import ask.me.again.meshinery.connectors.kafka.KafkaInputSource;
import ask.me.again.meshinery.connectors.kafka.KafkaOutputSource;
import ask.me.again.meshinery.connectors.kafka.KafkaProducerFactory;
import ask.me.again.meshinery.example.TestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public KafkaProducerFactory kafkaProducerFactory() {
    return new KafkaProducerFactory();
  }

  @Bean
  public KafkaConsumerFactory kafkaConsumerFactory() {
    return new KafkaConsumerFactory();
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(20);
  }

  @Bean
  public KafkaInputSource<TestContext> kafkaInputSource(ObjectMapper objectMapper, KafkaConsumerFactory kafkaConsumerFactory) {
    return new KafkaInputSource<>(TestContext.class, objectMapper, kafkaConsumerFactory);
  }

  @Bean
  public KafkaOutputSource<TestContext> kafkaOutputSource(ObjectMapper objectMapper, KafkaProducerFactory kafkaProducerFactory) {
    return new KafkaOutputSource<>(kafkaProducerFactory, objectMapper);
  }
}
