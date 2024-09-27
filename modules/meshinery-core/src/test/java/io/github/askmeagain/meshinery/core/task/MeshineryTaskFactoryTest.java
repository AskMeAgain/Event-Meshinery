package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeshineryTaskFactoryTest {

  @Test
  void testImmutability() {
    //Arrange --------------------------------------------------------------------------------
    var memoryConnector = new MemoryConnector<String, TestContext>();
    var baseTask = MeshineryTask.<String, TestContext>builder()
        .read("abc")
        .taskName("basename")
        .connector(memoryConnector);

    //Act ------------------------------------------------------------------------------------
    var task1 = baseTask.process((c) -> c).build();
    var task2 = baseTask.process((c) -> c).build();

    //Assert ---------------------------------------------------------------------------------
    //an internal processor is created aswell
    assertThat(task2).extracting(MeshineryTask::getProcessorList).asList().hasSize(2);
    assertThat(task1).extracting(MeshineryTask::getProcessorList).asList().hasSize(2);
  }


}
