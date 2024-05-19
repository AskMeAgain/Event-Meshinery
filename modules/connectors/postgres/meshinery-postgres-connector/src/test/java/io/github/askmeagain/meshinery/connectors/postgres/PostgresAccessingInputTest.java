package io.github.askmeagain.meshinery.connectors.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresAccessingInputTest extends AbstractPostgresTestBase {

  private static final String STATE = "Test";

  @Test
  void testAccessingInput() {
    //Arrange --------------------------------------------------------------------------------
    var jdbi = jdbi();

    var postgresProperties = postgresProperties();

    var input = new PostgresInputSource<>("default", new ObjectMapper(), jdbi, TestContext.class, postgresProperties);
    var output = new PostgresOutputSource<>("default", jdbi, TestContext.class, postgresProperties);

    var value1 = new TestContext(1);
    var value2 = new TestContext(2);

    //Act ------------------------------------------------------------------------------------
    output.writeOutput(STATE, value1);
    output.writeOutput(STATE + "2", value1);
    output.writeOutput(STATE, value2);

    var specificResult1 = input.getContext(STATE, value1.getId());
    var specificResultEmpty = input.getContext(STATE, value1.getId());
    var specificResultEmpty2 = input.getContext(STATE, "3");
    var specificResult2 = input.getContext(STATE + "2", value1.getId());

    //Assert ---------------------------------------------------------------------------------
    assertThat(specificResult1).contains(value1);
    assertThat(specificResult2).contains(value1);
    assertThat(specificResultEmpty).isEmpty();
    assertThat(specificResultEmpty2).isEmpty();
  }
}
