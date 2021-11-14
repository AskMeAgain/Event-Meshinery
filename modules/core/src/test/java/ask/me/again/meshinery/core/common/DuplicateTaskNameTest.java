package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.task.MeshineryTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuplicateTaskNameTest {

  @Test
  void happyCase() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var duplicateTask = MeshineryTask.builder()
        .taskName("duplicateTask");

    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThatThrownBy(() -> RoundRobinScheduler.builder()
        .task(duplicateTask)
        .task(duplicateTask)
        .buildAndStart()
    ).isInstanceOf(RuntimeException.class);
  }

}
