package io.github.askmeagain.meshinery.connectors.kafka.e2e;

import io.github.askmeagain.meshinery.connectors.kafka.EnableMeshineryKafka;
import io.github.askmeagain.meshinery.connectors.kafka.sources.KafkaConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.context.TestContext2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMeshineryKafka(context = {TestContext.class, TestContext2.class})
public class E2eKafkaTestConfiguration {
  @Bean
  public TestContext2 testContext2(KafkaConnector<TestContext> k1, KafkaConnector<TestContext2> k2) {
    return null;
  }
}
