package ask.me.again.meshinery.core.source;

import ask.me.again.meshinery.core.utils.context.TestContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MemoryConnectorTest {

  @Test
  void inputOutputTest() {
    //Arrange ---------------------------------------------------------------------------------
    var inputOutput = new MemoryConnector<String, TestContext>("default");
    var input = TestContext.builder()
        .id("2")
        .build();

    //Act -------------------------------------------------------------------------------------
    inputOutput.writeOutput("TestKey", input);
    var resultEmpty = inputOutput.getInputs("TestKey2");
    var result = inputOutput.getInputs("TestKey");
    var resultEmpty2 = inputOutput.getInputs("TestKey");

    //Assert ----------------------------------------------------------------------------------
    Assertions.assertThat(result).first().isEqualTo(input);
    Assertions.assertThat(resultEmpty).isEmpty();
    Assertions.assertThat(resultEmpty2).isEmpty();
  }
}