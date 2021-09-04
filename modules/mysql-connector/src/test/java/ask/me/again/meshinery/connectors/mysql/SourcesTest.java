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

    var input = new MysqlInputSource<>(jdbi, TestContext.class);
    var output = new MysqlOutputSource<>(jdbi, TestContext.class);
    var value = new TestContext("1");

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(STATE, value);
    var result = input.getInputs(STATE);

    //Assert ---------------------------------------------------------------------------------
    assertThat(result)
      .hasSize(1)
      .contains(value);
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestContext implements Context {
    String id;
  }
}
