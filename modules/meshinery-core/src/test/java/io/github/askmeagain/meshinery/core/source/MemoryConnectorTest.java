package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryConnectorTest {

  public static final String TEST_KEY = "TestKey";

  @Test
  void inputOutputTest() {
    //Arrange ---------------------------------------------------------------------------------
    var inputOutput = new MemoryConnector<String, TestContext>();
    var input = new TestContext(2);

    //Act -------------------------------------------------------------------------------------
    inputOutput.writeOutput(TEST_KEY, input);
    var resultEmpty = inputOutput.getInputs(List.of("TestKey2"));
    var result = inputOutput.getInputs(List.of(TEST_KEY));
    var resultEmpty2 = inputOutput.getInputs(List.of(TEST_KEY));

    //Assert ----------------------------------------------------------------------------------
    assertThat(result).first().isEqualTo(input);
    assertThat(resultEmpty).isEmpty();
    assertThat(resultEmpty2).isEmpty();
  }

  @Test
  void accessingInputTest() {
    //Arrange ---------------------------------------------------------------------------------
    var connector = new MemoryConnector<String, TestContext>();
    var input1 = new TestContext(1);
    var input2 = new TestContext(2);
    var input3 = new TestContext(3);
    var input4 = new TestContext(4);

    //Act -------------------------------------------------------------------------------------
    connector.writeOutput(TEST_KEY, input1);
    connector.writeOutput(TEST_KEY, input2);
    connector.writeOutput(TEST_KEY, input3);
    connector.writeOutput(TEST_KEY, input4);

    var result = connector.getContext(TEST_KEY, "3");
    var empty = connector.getContext(TEST_KEY, "5");

    //Assert ----------------------------------------------------------------------------------
    assertThat(result).contains(input3);
    assertThat(empty).isEmpty();
  }
}