package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class JoinTest {

  public static final String KEY = "Key";

  @Test
  void joinTest() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var leftSource = new MemoryConnector<String, TestContext>("default");
    var rightSource = new MemoryConnector<String, TestContext>("default");
    var joinedSource = new JoinedInnerInputSource<>("joined", leftSource, rightSource, KEY, this::combine, 10);

    //Act --------------------------------------------------------------------------------------------------------------
    leftSource.writeOutput(KEY, new TestContext(1), new TaskData());
    leftSource.writeOutput(KEY, new TestContext(2), new TaskData());

    rightSource.writeOutput(KEY, new TestContext(1), new TaskData());
    rightSource.writeOutput(KEY, new TestContext(2), new TaskData());
    rightSource.writeOutput(KEY, new TestContext(3), new TaskData());

    var result = joinedSource.getInputs(List.of(KEY));

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(result).hasSize(2)
        .contains(new TestContext(1), new TestContext(2));
  }

  @SneakyThrows
  @ParameterizedTest()
  @CsvSource({"1000,0", "500,1"})
  void joinTtlTest(int waitTime, int resultSize) {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var leftSource = new MemoryConnector<String, TestContext>("default");
    var rightSource = new MemoryConnector<String, TestContext>("default");
    var joinedSource = new JoinedInnerInputSource<>("joined", leftSource, rightSource, KEY, this::combine, 1);

    //Act --------------------------------------------------------------------------------------------------------------
    leftSource.writeOutput(KEY, new TestContext(1), new TaskData());
    leftSource.writeOutput(KEY, new TestContext(2), new TaskData());

    rightSource.writeOutput(KEY, new TestContext(1), new TaskData());

    var result = joinedSource.getInputs(List.of(KEY));

    Thread.sleep(waitTime);

    rightSource.writeOutput(KEY, new TestContext(2), new TaskData());
    rightSource.writeOutput(KEY, new TestContext(3), new TaskData());

    var resultEmpty = joinedSource.getInputs(List.of(KEY));

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(result).contains(new TestContext(1));
    assertThat(resultEmpty).hasSize(resultSize);
  }

  private TestContext combine(TestContext left, TestContext right) {
    assertThat(left.getId()).isEqualTo(right.getId());
    return right;
  }
}