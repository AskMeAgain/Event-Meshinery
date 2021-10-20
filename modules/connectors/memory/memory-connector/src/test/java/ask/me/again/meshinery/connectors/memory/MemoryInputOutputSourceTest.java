package ask.me.again.meshinery.connectors.memory;

import ask.me.again.meshinery.core.common.Context;
import lombok.Builder;
import lombok.Value;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryInputOutputSourceTest {

  @Test
  void inputOutputTest() {
    //Arrange ---------------------------------------------------------------------------------
    var inputOutput = new MemoryInputOutputSource<String, Context>();
    var input = TestContext.builder()
        .id("2")
        .build();

    //Act -------------------------------------------------------------------------------------
    inputOutput.writeOutput("TestKey", input);
    var resultEmpty = inputOutput.getInputs("TestKey2");
    var result = inputOutput.getInputs("TestKey");
    var resultEmpty2 = inputOutput.getInputs("TestKey");

    //Assert ----------------------------------------------------------------------------------
    assertThat(result).first().isEqualTo(input);
    assertThat(resultEmpty).isNull();
    assertThat(resultEmpty2).isNull();
  }

  @Value
  @Builder
  private static class TestContext implements Context {
    String id;
  }
}