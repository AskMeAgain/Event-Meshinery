package ask.me.again.meshinery.core.source.memory;

import ask.me.again.meshinery.core.common.Context;
import lombok.Builder;
import lombok.Value;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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
    Assertions.assertThat(result).first().isEqualTo(input);
    Assertions.assertThat(resultEmpty).isNull();
    Assertions.assertThat(resultEmpty2).isNull();
  }

  @Value
  @Builder
  private static class TestContext implements Context {
    String id;
  }
}