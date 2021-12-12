package io.github.askmeagain.meshinery.connectors.kafka.e2e;

import io.github.askmeagain.meshinery.connectors.kafka.EnableMeshineryKafkaConnector;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootApplication
@EnableMeshinery
@EnableMeshineryKafkaConnector
@Import({E2eTestConfiguration.class})
public class E2eTestApplication {

}
