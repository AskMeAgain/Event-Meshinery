package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.connectors.kafka.KafkaConfiguration;
import ask.me.again.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import ask.me.again.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import ask.me.again.meshinery.connectors.kafka.sources.KafkaInputSource;
import ask.me.again.meshinery.connectors.kafka.sources.KafkaOutputSource;
import ask.me.again.meshinery.example.TestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaConfiguration.class)
@SuppressWarnings("checkstyle:MissingJavadocType")
public class ApplicationConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(20);
  }

  @Bean
  public KafkaInputSource<TestContext> kafkaInputSource(
      ObjectMapper objectMapper,
      KafkaConsumerFactory kafkaConsumerFactory
  ) {
    return new KafkaInputSource<>("default", TestContext.class, objectMapper, kafkaConsumerFactory);
  }

  @Bean
  public KafkaOutputSource<TestContext> kafkaOutputSource(
      ObjectMapper objectMapper,
      KafkaProducerFactory kafkaProducerFactory
  ) {
    return new KafkaOutputSource<>("default", kafkaProducerFactory, objectMapper);
  }

}
