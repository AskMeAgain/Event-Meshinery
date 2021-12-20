package io.github.askmeagain.meshinery.connectors.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MysqlConnectorTest extends AbstractMysqlTestBase {

  private static final String STATE = "Test";

  @Test
  void testMysqlConnector() {
    //Arrange --------------------------------------------------------------------------------
    var mysqlConnector = new MysqlConnector<>(TestContext.class, new ObjectMapper(), meshineryMysqlProperties());
    var value1 = new TestContext(1);
    var value2 = new TestContext(2);

    //Act ------------------------------------------------------------------------------------
    mysqlConnector.writeOutput(STATE, value1);
    mysqlConnector.writeOutput(STATE, value2);

    var result1 = mysqlConnector.getInputs(List.of(STATE));
    var result2 = mysqlConnector.getInputs(List.of(STATE));

    //Assert ---------------------------------------------------------------------------------
    assertThat(result1)
        .hasSize(1)
        .contains(value1);
    assertThat(result2)
        .hasSize(1)
        .contains(value2);
  }
}
