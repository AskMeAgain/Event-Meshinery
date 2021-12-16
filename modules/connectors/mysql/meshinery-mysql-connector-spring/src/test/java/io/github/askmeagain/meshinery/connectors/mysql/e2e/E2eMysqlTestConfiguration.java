package io.github.askmeagain.meshinery.connectors.mysql.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.mysql.EnableMeshineryMysqlConnector;
import io.github.askmeagain.meshinery.connectors.mysql.MeshineryMysqlProperties;
import io.github.askmeagain.meshinery.connectors.mysql.MysqlConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.jdbi.v3.core.Jdbi;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
@EnableMeshineryMysqlConnector
public class E2eMysqlTestConfiguration {

  @Bean
  public MysqlConnector<TestContext> kafkaConnector(
      Jdbi jdbi, ObjectMapper objectMapper, MeshineryMysqlProperties mysqlProperties
  ) {
    return new MysqlConnector<>("name", TestContext.class, jdbi, objectMapper, mysqlProperties);
  }
}
