package ask.me.again.meshinery.core.source;

import ask.me.again.meshinery.core.utils.context.TestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignalingInputSourceTest {

  public static final String KEY = "";

  @Test
  void signalingInputTest() {
    //Arrange --------------------------------------------------------------------------------
    var signal = new MemoryConnector<String, TestContext>();
    var resultSource = new MemoryConnector<String, TestContext>();

    resultSource.writeOutput(KEY, new TestContext(1));
    resultSource.writeOutput("1", new TestContext(0));

    var signalSource = new SignalingInputSource<>("signal", signal, resultSource);

    //Act ------------------------------------------------------------------------------------
    var empty1 = signalSource.getInputs(KEY);
    
    signal.writeOutput(KEY, new TestContext(1234));

    var result = signalSource.getInputs(KEY);
    var empty2 = signalSource.getInputs(KEY);

    signal.writeOutput(KEY, new TestContext(1234));

    var empty3 = signalSource.getInputs(KEY);

    //Assert ---------------------------------------------------------------------------------
    assertThat(result).contains(new TestContext(1));
    assertThat(empty1).isEmpty();
    assertThat(empty2).isEmpty();
    assertThat(empty3).isEmpty();
  }
}
