package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SourcesTest extends AbstractMysqlTest {

  public static final String STATE = "Test";

  @Test
  void testInputOutput() {
    //Arrange --------------------------------------------------------------------------------
    var jdbi = jdbi();

    var mysqlProperties = new MysqlProperties();
    mysqlProperties.setLimit(1);
    var input = new MysqlInputSource<>(jdbi, TestContext.class, mysqlProperties);
    var output = new MysqlOutputSource<>(jdbi, TestContext.class);
    var value1 = new TestContext("1");
    var value2 = new TestContext("2");

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(STATE, value1);
    output.writeOutput(STATE, value2);

    var result1 = input.getInputs(STATE);
    var result2 = input.getInputs(STATE);

    //Assert ---------------------------------------------------------------------------------
    assertThat(result1)
        .hasSize(1)
        .contains(value1);
    assertThat(result2)
        .hasSize(1)
        .contains(value2);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestContext implements Context {
    String id;
  }
}
