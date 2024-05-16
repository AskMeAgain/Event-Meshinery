package io.github.askmeagain.meshinery.aop.e2e.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class E2eTestConfiguration {

  @Bean
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(1);
  }

  @Bean
  public MeshineryConnector<String, DataContext> connector() {
    return new MemoryConnector<>();
  }

}
