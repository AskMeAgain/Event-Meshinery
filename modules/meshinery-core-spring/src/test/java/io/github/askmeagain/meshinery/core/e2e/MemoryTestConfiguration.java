package io.github.askmeagain.meshinery.core.e2e;

import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MemoryTestConfiguration {

  @Bean
  public MemoryConnector<String, TestContext> memoryConnector() {
    return new MemoryConnector<>();
  }
}
