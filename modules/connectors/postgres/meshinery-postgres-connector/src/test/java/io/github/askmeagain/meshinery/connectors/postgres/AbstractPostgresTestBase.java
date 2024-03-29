package io.github.askmeagain.meshinery.connectors.postgres;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public abstract class AbstractPostgresTestBase {

  private static final String DB_NAME = "db";

  private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres")
      .withDatabaseName(DB_NAME)
      .withPassword("password")
      .withUsername("user")
      .withInitScript("postgres.ddl")
      .waitingFor(Wait.forLogMessage(".*is ready to accept connections.*", 1));

  @BeforeAll
  protected static void setup() {
    postgresContainer.start();
  }

  @DynamicPropertySource
  static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    registry.add(
        "meshinery.connectors.postgres.connection-string",
        AbstractPostgresTestBase::getConnectingString
    );
  }

  @AfterEach
  protected void truncate() {
    jdbi().useHandle(handle -> handle.createCall("TRUNCATE testcontext").invoke());
  }

  protected Jdbi jdbi() {

    var jdbi = Jdbi.create(
        getConnectingString(),
        postgresContainer.getUsername(),
        postgresContainer.getPassword()
    );
    jdbi.installPlugin(new Jackson2Plugin());

    return jdbi;
  }

  private static String getConnectingString() {
    return postgresContainer.getJdbcUrl() + "?useSSL=false";
  }

  protected MeshineryPostgresProperties meshineryMysqlProperties() {
    var meshineryPostgresProperties = new MeshineryPostgresProperties();

    meshineryPostgresProperties.setLimit(1);
    meshineryPostgresProperties.setPassword(postgresContainer.getPassword());
    meshineryPostgresProperties.setUser(postgresContainer.getUsername());
    meshineryPostgresProperties.setConnectionString(getConnectingString());

    return meshineryPostgresProperties;
  }
}
