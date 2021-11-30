package io.github.askmeagain.meshinery.connectors.mysql;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputsTest extends AbstractMysqlTest {

  public static final String STATE = "Test";

  @Test
  void testInputOutput() {
    //Arrange --------------------------------------------------------------------------------
    var jdbi = jdbi();

    var mysqlProperties = new MysqlProperties();
    mysqlProperties.setLimit(1);
    var input = new MysqlInputSource<>("default", jdbi, TestContext.class, mysqlProperties);
    var output = new MysqlOutputSource<>("default", jdbi, TestContext.class);
    var value1 = new TestContext(1);
    var value2 = new TestContext(2);

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

  @Test
  void testOverrideInputOutput() {
    //Arrange --------------------------------------------------------------------------------
    var jdbi = jdbi();

    var mysqlProperties = new MysqlProperties();
    mysqlProperties.setLimit(1);
    var input = new MysqlInputSource<>("default", jdbi, TestContext.class, mysqlProperties);
    var output = new MysqlOutputSource<>("default", jdbi, TestContext.class);
    var value1 = new TestContext(1);
    var value2 = new TestContext(2);

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(STATE, value1);
    var result1 = input.getInputs(STATE);

    TaskData.setTaskData(new TaskData().put(MysqlProperties.MYSQL_OVERRIDE_EXISTING, ""));
    output.writeOutput(STATE, value2);
    var result2 = input.getInputs(STATE);

    //Assert ---------------------------------------------------------------------------------
    assertThat(result1)
        .hasSize(1)
        .contains(value1);
    assertThat(result2)
        .hasSize(1)
        .contains(value2);
  }


  @Test
  void testDuplicateInput() {
    //Arrange --------------------------------------------------------------------------------
    var jdbi = jdbi();

    var output = new MysqlOutputSource<>("default", jdbi, TestContext.class);
    var value1 = new TestContext(1);

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(STATE, value1);

    //Assert ---------------------------------------------------------------------------------
    assertThatThrownBy(() -> output.writeOutput(STATE, value1))
        .isInstanceOf(RuntimeException.class);
  }
}
