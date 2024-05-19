package io.github.askmeagain.meshinery.connectors.postgres;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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

  @AfterEach
  protected void truncate() {
    jdbi().useHandle(handle -> handle.createCall("TRUNCATE db.testcontext").invoke());
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

  protected static String getConnectingString() {
    return postgresContainer.getJdbcUrl() + "?useSSL=false";
  }

  protected MeshineryPostgresProperties postgresProperties() {
    var postgresProperties = new MeshineryPostgresProperties();

    postgresProperties.setLimit(1);
    postgresProperties.setSchema("db");
    postgresProperties.setPassword(postgresContainer.getPassword());
    postgresProperties.setUser(postgresContainer.getUsername());
    postgresProperties.setConnectionString(getConnectingString());

    return postgresProperties;
  }
}
