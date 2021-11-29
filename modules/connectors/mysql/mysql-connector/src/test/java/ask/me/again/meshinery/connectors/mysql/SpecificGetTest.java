package ask.me.again.meshinery.connectors.mysql;

import ask.me.again.meshinery.core.common.DataContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpecificGetTest extends AbstractMysqlTest {

  public static final String STATE = "Test";

  @Test
  void testSpecificInput() {
    //Arrange --------------------------------------------------------------------------------
    var jdbi = jdbi();

    var mysqlProperties = new MysqlProperties();
    mysqlProperties.setLimit(1);
    var input = new MysqlInputSource<>("default", jdbi, TestContext.class, mysqlProperties);
    var output = new MysqlOutputSource<>("default", jdbi, TestContext.class);

    var value1 = new TestContext("1");
    var value2 = new TestContext("2");

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(STATE, value1);
    output.writeOutput(STATE, value2);

    var specificResult = input.getContext(STATE, "1");
    var specificResultEmpty = input.getContext(STATE, "1");
    var specificResultEmpty2 = input.getContext(STATE, "3");

    //Assert ---------------------------------------------------------------------------------
    assertThat(specificResult).contains(value1);
    assertThat(specificResultEmpty).isEmpty();
    assertThat(specificResultEmpty2).isEmpty();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TestContext implements DataContext {
    String id;
  }
}
