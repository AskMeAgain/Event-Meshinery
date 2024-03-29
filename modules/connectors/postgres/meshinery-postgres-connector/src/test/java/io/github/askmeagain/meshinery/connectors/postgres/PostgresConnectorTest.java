package io.github.askmeagain.meshinery.connectors.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresConnectorTest extends AbstractPostgresTestBase {

  private static final String STATE = "Test";

  @Test
  void testMysqlConnector() {
    //Arrange --------------------------------------------------------------------------------
    var postgresConnector = new PostgresConnector<>(TestContext.class, new ObjectMapper(), meshineryMysqlProperties());
    var value1 = new TestContext(1);
    var value2 = new TestContext(2);

    //Act ------------------------------------------------------------------------------------
    postgresConnector.writeOutput(STATE, value1);
    postgresConnector.writeOutput(STATE, value2);

    var result1 = postgresConnector.getInputs(List.of(STATE));
    var result2 = postgresConnector.getInputs(List.of(STATE));

    //Assert ---------------------------------------------------------------------------------
    assertThat(result1)
        .hasSize(1)
        .contains(value1);
    assertThat(result2)
        .hasSize(1)
        .contains(value2);
  }
}
