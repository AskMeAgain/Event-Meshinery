package ask.me.again.meshinery.connectors.mysql;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class AbstractMysqlTest {


  public static String DB_NAME = "db";

  public static GenericContainer mySQLContainer = new MySQLContainer("mysql")
    .withDatabaseName(DB_NAME)
    .withPassword("password")
    .withUsername("user")
    .withInitScript("mysql.ddl")
    .waitingFor(Wait.forLogMessage(".*ready for connections.*", 1));

  @BeforeAll
  public static void setup() {
    mySQLContainer.start();
  }

  public Jdbi jdbi() {
    var casted = (MySQLContainer) mySQLContainer;

    return Jdbi.create(casted.getJdbcUrl() + "?useSSL=false", casted.getUsername(), casted.getPassword());
  }
}
