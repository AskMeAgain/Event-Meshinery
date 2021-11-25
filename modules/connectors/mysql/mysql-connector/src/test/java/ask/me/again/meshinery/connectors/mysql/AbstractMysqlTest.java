package ask.me.again.meshinery.connectors.mysql;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class AbstractMysqlTest {


  public static String DB_NAME = "db";

  public static GenericContainer mySQLContainer = new MySQLContainer<>("mysql")
      .withDatabaseName(DB_NAME)
      .withPassword("password")
      .withUsername("user")
      .withInitScript("mysql.ddl")
      .waitingFor(Wait.forLogMessage(".*ready for connections.*", 1));

  @BeforeAll
  public static void setup() {
    mySQLContainer.start();
  }

  @BeforeEach
  @AfterEach
  public void truncate(){
    jdbi().useHandle(handle -> handle.createCall("TRUNCATE db.TestContext").invoke());
  }

  public Jdbi jdbi() {
    var container = (MySQLContainer) mySQLContainer;

    var jdbi = Jdbi.create(container.getJdbcUrl() + "?useSSL=false", container.getUsername(), container.getPassword());
    jdbi.installPlugin(new Jackson2Plugin());

    return jdbi;
  }
}
