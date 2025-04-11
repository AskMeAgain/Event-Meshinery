package io.github.askmeagain.meshinery.core.scheduler;

import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class BackoffTimeTest {

  @Test
  void testBackoffTime() throws InterruptedException {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var processor = Mockito.spy(new TestContextProcessor(0));
    MeshinerySourceConnector<?, TestContext> inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .iterations(3)
        .build();
    var inputSourceSpy = Mockito.spy(inputSource);
    var task = MeshineryTask.<Object, TestContext>builder()
        .inputSource((MeshineryInputSource<Object, TestContext>) inputSourceSpy)
        .read("")
        .backoffTime(180)
        .process(processor)
        .build()
        .initialize();
    var scheduler = RoundRobinScheduler.builder().build();

    //Act --------------------------------------------------------------------------------------------------------------
    var result1 = scheduler.getNewTaskRuns(task);
    var result2 = scheduler.getNewTaskRuns(task);

    Thread.sleep(200);

    var result3 = scheduler.getNewTaskRuns(task);
    var result4 = scheduler.getNewTaskRuns(task);

    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(result1).hasSize(1);
    assertThat(result2).isEmpty();
    assertThat(result3).hasSize(1);
    assertThat(result4).isEmpty();

    Mockito.verify(inputSourceSpy, Mockito.times(2)).getInputs(any());

  }
}
