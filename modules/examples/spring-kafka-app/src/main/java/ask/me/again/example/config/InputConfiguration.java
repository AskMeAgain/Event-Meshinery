package ask.me.again.example.config;

import ask.me.again.example.TestContext;
import ask.me.again.kafka.KafkaInputSource;
import ask.me.again.kafka.KafkaOutputSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class InputConfiguration {

  @Bean
  public ObjectMapper objectMapper(){
    return new ObjectMapper();
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(20);
  }

  @Bean
  public KafkaOutputSource<TestContext> kafkaOutputSource(ObjectMapper objectMapper) {
    return new KafkaOutputSource<>(objectMapper);
  }

  @Bean
  public KafkaInputSource<TestContext> kafkaInputSource(ObjectMapper objectMapper) {
    return new KafkaInputSource<>(TestContext.class, objectMapper);
  }
}
