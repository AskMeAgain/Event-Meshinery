package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StaticInputSourceTest {

  private static final TestContext EXPECTED = TestContext.builder().index(123).build();
  private static final TestContext EXPECTED2 = TestContext.builder().index(12345).build();

  @Test
  public void sourceTest() {
    //Arrange --------------------------------------------------------------------------------
    var source = new StaticInputSource<String, TestContext>("name", keys -> List.of(
        EXPECTED,
        EXPECTED2,
        EXPECTED2.toBuilder().id(String.join("-", keys)).build()
    ));

    //Act ------------------------------------------------------------------------------------
    var result = source.getInputs(List.of("abc", "123"));
    var result2 = source.getInputs(List.of("abc", "123"));
    var result3 = source.getInputs(List.of("def", "456"));

    //Assert ---------------------------------------------------------------------------------
    assertThat(result3).containsExactly(EXPECTED, EXPECTED2, EXPECTED2.toBuilder().id("def-456").build());
    assertThat(result).containsExactly(EXPECTED, EXPECTED2, EXPECTED2.toBuilder().id("abc-123").build())
        .isEqualTo(result2);
  }
}