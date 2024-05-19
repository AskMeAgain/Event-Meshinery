package io.github.askmeagain.meshinery.connectors.postgres;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class AbstractSpringPostgresTestBase extends AbstractPostgresTestBase {

  @DynamicPropertySource
  static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    registry.add(
        "meshinery.connectors.postgres.connection-string",
        AbstractPostgresTestBase::getConnectingString
    );
  }
}
