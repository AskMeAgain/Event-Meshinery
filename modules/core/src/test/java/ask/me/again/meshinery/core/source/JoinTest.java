package ask.me.again.meshinery.core.source;

import ask.me.again.meshinery.core.common.context.TestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JoinTest {

  public static final String KEY = "Key";

  @Test
  void joinTest() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var leftSource = new MemoryInputOutputSource<String, TestContext>();
    var rightSource = new MemoryInputOutputSource<String, TestContext>();
    var joinedSource = new JoinedInputSource<>(leftSource, rightSource, KEY, this::combine);

    //Act --------------------------------------------------------------------------------------------------------------
    leftSource.writeOutput(KEY, new TestContext(1));
    leftSource.writeOutput(KEY, new TestContext(2));

    rightSource.writeOutput(KEY, new TestContext(1));
    rightSource.writeOutput(KEY, new TestContext(2));
    rightSource.writeOutput(KEY, new TestContext(3));

    var result = joinedSource.getInputs(KEY);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(result).hasSize(2);
  }

  private TestContext combine(TestContext left, TestContext right) {
    assertThat(left.getId()).isEqualTo(right.getId());
    return null;
  }


}
