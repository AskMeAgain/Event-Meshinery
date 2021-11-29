package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.core.common.DataContext;
import ask.me.again.meshinery.core.source.MemoryConnector;
import ask.me.again.meshinery.example.entities.VoteContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings("checkstyle:MissingJavadocType")
public class InputConfiguration {

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(20);
  }

  @Bean
  public MemoryConnector<String, DataContext> memoryInputOutputSource() {
    return new MemoryConnector<>("rest-input");
  }

  @Bean
  public MemoryConnector<String, VoteContext> voteMemorySource() {
    return new MemoryConnector<>("voting-context");
  }
}
