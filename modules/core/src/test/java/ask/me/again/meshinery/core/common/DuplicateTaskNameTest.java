package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.task.MeshineryTaskFactory;
import ask.me.again.meshinery.core.utils.context.TestContext;
import ask.me.again.meshinery.core.utils.sources.TestInputSource;
import ask.me.again.meshinery.core.utils.sources.TestOutputSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuplicateTaskNameTest {

  @Test
  void duplicateName() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var duplicateTask = MeshineryTaskFactory.builder()
        .taskName("duplicateTask")
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThatThrownBy(() -> RoundRobinScheduler.builder()
        .task(duplicateTask)
        .task(duplicateTask)
        .buildAndStart()
    ).isInstanceOf(RuntimeException.class)
        .hasMessage("Found duplicate job names: [duplicateTask]");
  }

  @Test
  void duplicateReadKey() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var inputSource = TestInputSource.builder().build();
    var outputSource = new TestOutputSource();
    var duplicateTask1 = MeshineryTaskFactory.<String, TestContext>builder()
        .taskName("task1")
        .inputSource(inputSource)
        .read("abc", null)
        .defaultOutputSource(outputSource)
        .build();
    var duplicateTask2 = MeshineryTaskFactory.<String, TestContext>builder()
        .taskName("task2")
        .inputSource(inputSource)
        .defaultOutputSource(outputSource)
        .read("abc", null)
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThatThrownBy(() -> RoundRobinScheduler.builder()
        .task(duplicateTask1)
        .task(duplicateTask2)
        .buildAndStart()
    ).isInstanceOf(RuntimeException.class)
        .hasMessage("Found duplicate Read keys: [abc]");
  }

}
