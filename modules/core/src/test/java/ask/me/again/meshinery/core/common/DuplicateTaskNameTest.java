package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.task.MeshineryTaskFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuplicateTaskNameTest {

  @Test
  void happyCase() {
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
    ).isInstanceOf(RuntimeException.class);
  }

}
