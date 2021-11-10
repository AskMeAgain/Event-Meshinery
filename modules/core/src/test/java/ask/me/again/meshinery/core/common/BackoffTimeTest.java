package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.utils.context.TestContext;
import ask.me.again.meshinery.core.utils.processor.TestContextProcessor;
import ask.me.again.meshinery.core.utils.sources.TestInputSource;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class BackoffTimeTest {

  @Test
  void testBackoffTime() throws InterruptedException {

    //Arrange ----------------------------------------------------------------------------------------------------------
    var executor = Executors.newFixedThreadPool(11);
    var processor = Mockito.spy(new TestContextProcessor(0));
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .iterations(3)
        .build();

    var inputSourceSpy = Mockito.spy(inputSource);

    var task = MeshineryTask.<String, TestContext>builder()
        .inputSource(inputSourceSpy)
        .read("", executor)
        .backoffTime(180)
        .process(processor);

    //Act --------------------------------------------------------------------------------------------------------------
    var result1 = task.getInputValues();
    var result2 = task.getInputValues();

    Thread.sleep(200);

    var result3 = task.getInputValues();
    var result4 = task.getInputValues();

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(result1).hasSize(1);
    assertThat(result3).hasSize(1);

    assertThat(result2).isEmpty();
    assertThat(result4).isEmpty();

    Mockito.verify(inputSourceSpy,Mockito.times(2)).getInputs(any());

  }
}
