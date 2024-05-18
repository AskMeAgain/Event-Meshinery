package io.github.askmeagain.meshinery.connectors.mysql;

import io.github.askmeagain.meshinery.connectors.postgres.AbstractPostgresTestBase;
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
