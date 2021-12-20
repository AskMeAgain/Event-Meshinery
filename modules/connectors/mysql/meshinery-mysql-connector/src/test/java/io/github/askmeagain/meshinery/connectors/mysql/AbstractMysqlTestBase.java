package io.github.askmeagain.meshinery.connectors.mysql;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public abstract class AbstractMysqlTestBase {

  private static final String DB_NAME = "db";

  private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql")
      .withDatabaseName(DB_NAME)
      .withPassword("password")
      .withUsername("user")
      .withInitScript("mysql.ddl")
      .waitingFor(Wait.forLogMessage(".*ready for connections.*", 1));

  @BeforeAll
  protected static void setup() {
    mysqlContainer.start();
  }

  @DynamicPropertySource
  static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    registry.add(
        "meshinery.connectors.mysql.connection-string",
        AbstractMysqlTestBase::getConnectingString
    );
  }

  @AfterEach
  protected void truncate() {
    jdbi().useHandle(handle -> handle.createCall("TRUNCATE db.TestContext").invoke());
  }

  protected Jdbi jdbi() {

    var jdbi = Jdbi.create(
        getConnectingString(),
        mysqlContainer.getUsername(),
        mysqlContainer.getPassword()
    );
    jdbi.installPlugin(new Jackson2Plugin());

    return jdbi;
  }

  private static String getConnectingString() {
    return mysqlContainer.getJdbcUrl() + "?useSSL=false";
  }

  protected MeshineryMysqlProperties meshineryMysqlProperties() {
    var meshineryMysqlProperties = new MeshineryMysqlProperties();

    meshineryMysqlProperties.setLimit(1);
    meshineryMysqlProperties.setPassword(mysqlContainer.getPassword());
    meshineryMysqlProperties.setUser(mysqlContainer.getUsername());
    meshineryMysqlProperties.setConnectionString(getConnectingString());

    return meshineryMysqlProperties;
  }
}
