package io.github.askmeagain.meshinery.connectors.mysql;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class AbstractSpringMysqlTestBase extends AbstractMysqlTestBase {

  @DynamicPropertySource
  static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    registry.add(
        "meshinery.connectors.mysql.connection-string",
        AbstractMysqlTestBase::getConnectingString
    );
  }
}
