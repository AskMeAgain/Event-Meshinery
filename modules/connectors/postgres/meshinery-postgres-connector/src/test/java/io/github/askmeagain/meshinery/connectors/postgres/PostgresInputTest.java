package io.github.askmeagain.meshinery.connectors.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostgresInputTest extends AbstractPostgresTestBase {

  private static final String STATE = "Test";

  @Test
  void testInputOutput() {
    //Arrange --------------------------------------------------------------------------------
    var jdbi = jdbi();

    var postgresProperties = postgresProperties();

    var input = new PostgresInputSource<>("default", new ObjectMapper(), jdbi, TestContext.class, postgresProperties);
    var output = new PostgresOutputSource<>("default", jdbi, TestContext.class, postgresProperties);
    var value1 = new TestContext(1);
    var value2 = new TestContext(2);

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(STATE, value1);
    output.writeOutput(STATE, value2);

    var result1 = input.getInputs(List.of(STATE));
    var result2 = input.getInputs(List.of(STATE));

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

    var postgresProperties = postgresProperties();

    var input = new PostgresInputSource<>("default", new ObjectMapper(), jdbi, TestContext.class, postgresProperties);
    var output = new PostgresOutputSource<>("default", jdbi, TestContext.class, postgresProperties);
    var value1 = new TestContext(1);
    var value2 = new TestContext(2);

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(STATE, value1);
    var result1 = input.getInputs(List.of(STATE));

    TaskData.setTaskData(new TaskData().put(MeshineryPostgresProperties.POSTGRES_OVERRIDE_EXISTING, ""));
    output.writeOutput(STATE, value2);
    var result2 = input.getInputs(List.of(STATE));

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

    var output = new PostgresOutputSource<>("default", jdbi, TestContext.class, postgresProperties());
    var value1 = new TestContext(1);

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(STATE, value1);

    //Assert ---------------------------------------------------------------------------------
    assertThatThrownBy(() -> output.writeOutput(STATE, value1))
        .isInstanceOf(UnableToExecuteStatementException.class);
  }
}
