package io.github.askmeagain.meshinery.core.source;

import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignalingInputSourceTest {

  private static final String KEY = "";

  @Test
  void signalingInputTest() {
    //Arrange --------------------------------------------------------------------------------
    var signal = new MemoryConnector<String, TestContext>();
    var resultSource = new MemoryConnector<String, TestContext>();

    var signalSource = new SignalingInputSource<>(false, "signal", signal, resultSource, KEY);

    resultSource.writeOutput(KEY, new TestContext(1));
    resultSource.writeOutput("1", new TestContext(0));

    //Act ------------------------------------------------------------------------------------
    var empty1 = signalSource.getInputs(List.of(KEY));

    signal.writeOutput(KEY, new TestContext(1234));

    var result = signalSource.getInputs(List.of(KEY));
    var empty2 = signalSource.getInputs(List.of(KEY));

    signal.writeOutput(KEY, new TestContext(1234));

    var empty3 = signalSource.getInputs(List.of(KEY));

    //Assert ---------------------------------------------------------------------------------
    assertThat(result).contains(new TestContext(1));
    assertThat(empty1).isEmpty();
    assertThat(empty2).isEmpty();
    assertThat(empty3).isEmpty();
  }


  @Test
  void signalingLockIn() {
    //Arrange --------------------------------------------------------------------------------
    var signal = new MemoryConnector<String, TestContext>();
    var resultSource = new MemoryConnector<String, TestContext>();

    var signalSource = new SignalingInputSource<>(true, "signal", signal, resultSource, KEY);

    resultSource.writeOutput(KEY, new TestContext(1));


    //Act ------------------------------------------------------------------------------------
    var empty1 = signalSource.getInputs(List.of(KEY));
    signal.writeOutput(KEY, new TestContext(1234));

    var result1 = signalSource.getInputs(List.of(KEY));

    resultSource.writeOutput(KEY, new TestContext(2));
    var result2 = signalSource.getInputs(List.of(KEY));

    resultSource.writeOutput(KEY, new TestContext(3));
    var result3 = signalSource.getInputs(List.of(KEY));

    resultSource.writeOutput(KEY, new TestContext(4));
    var result4 = signalSource.getInputs(List.of(KEY));

    resultSource.writeOutput("1", new TestContext(0));
    var empty2 = signalSource.getInputs(List.of(KEY));

    signal.writeOutput(KEY, new TestContext(1234));

    var empty3 = signalSource.getInputs(List.of(KEY));

    //Assert ---------------------------------------------------------------------------------
    assertThat(result1).contains(new TestContext(1));
    assertThat(result2).contains(new TestContext(2));
    assertThat(result3).contains(new TestContext(3));
    assertThat(result4).contains(new TestContext(4));
    assertThat(empty1).isEmpty();
    assertThat(empty2).isEmpty();
    assertThat(empty3).isEmpty();
  }
}
