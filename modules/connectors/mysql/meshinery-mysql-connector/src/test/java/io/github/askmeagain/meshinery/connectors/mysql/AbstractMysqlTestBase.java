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

  private static final MySQLContainer mysqlContainer = new MySQLContainer<>("mysql")
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
        () -> mysqlContainer.getJdbcUrl() + "?useSSL=false"
    );
  }

  @AfterEach
  protected void truncate() {
    jdbi().useHandle(handle -> handle.createCall("TRUNCATE db.TestContext").invoke());
  }

  protected Jdbi jdbi() {
    var container = (MySQLContainer) mysqlContainer;

    var jdbi = Jdbi.create(container.getJdbcUrl() + "?useSSL=false", container.getUsername(), container.getPassword());
    jdbi.installPlugin(new Jackson2Plugin());

    return jdbi;
  }
}
