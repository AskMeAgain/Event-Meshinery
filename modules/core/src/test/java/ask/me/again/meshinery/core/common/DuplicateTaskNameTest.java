package ask.me.again.meshinery.core.common;

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
        .build()
    ).isInstanceOf(RuntimeException.class);
  }

}
