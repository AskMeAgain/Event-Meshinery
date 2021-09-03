package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SourcesTest {

  public static final String STATE = "Test";

  @Test
  void testInputOutput() {
    //Arrange --------------------------------------------------------------------------------
    var jdbi = Jdbi.create("jdbc:mysql://localhost:3306/db?useSSL=false", "user", "password");
    var objectMapper = new ObjectMapper();
    var input = new MysqlInputSource<>(jdbi, TestContext.class, objectMapper);
    var output = new MysqlOutputSource<>(jdbi, TestContext.class, objectMapper);
    var value = new TestContext();
    value.setId("1");

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
  public static class TestContext implements Context {
    String id;
  }
}
