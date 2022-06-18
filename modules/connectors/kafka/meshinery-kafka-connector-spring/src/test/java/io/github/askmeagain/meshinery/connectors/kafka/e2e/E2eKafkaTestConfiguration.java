package io.github.askmeagain.meshinery.connectors.kafka.e2e;

import io.github.askmeagain.meshinery.connectors.kafka.EnableMeshineryKafka;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableMeshineryKafka(context = TestContext.class)
public class E2eKafkaTestConfiguration {

}
