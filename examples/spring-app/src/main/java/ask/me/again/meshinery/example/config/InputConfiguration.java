package ask.me.again.meshinery.example.config;

import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.source.MemoryInputOutputSource;
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
  public MemoryInputOutputSource<String, Context> memoryInputOutputSource() {
    return new MemoryInputOutputSource<>();
  }

  @Bean
  public MemoryInputOutputSource<String, VoteContext> voteMemorySource() {
    return new MemoryInputOutputSource<>();
  }
}
