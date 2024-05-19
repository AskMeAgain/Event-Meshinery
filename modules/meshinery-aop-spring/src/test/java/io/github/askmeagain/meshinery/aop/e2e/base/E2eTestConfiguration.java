package io.github.askmeagain.meshinery.aop.e2e.base;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class E2eTestConfiguration {

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(1);
  }

}
