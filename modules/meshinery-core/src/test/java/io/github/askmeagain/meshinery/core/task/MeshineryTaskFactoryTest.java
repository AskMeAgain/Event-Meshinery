package io.github.askmeagain.meshinery.core.task;

import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeshineryTaskFactoryTest {

  @Test
  void testImmutability() {
    //Arrange --------------------------------------------------------------------------------
    var memoryConnector = new MemoryConnector<String, TestContext>();
    var baseTask = MeshineryTaskFactory.<String, TestContext>builder()
        .read(null, "abc")
        .taskName("basename")
        .connector(memoryConnector);

    //Act ------------------------------------------------------------------------------------
    var task1 = baseTask.process((c, e) -> CompletableFuture.completedFuture(c)).build();
    var task2 = baseTask.process((c, e) -> CompletableFuture.completedFuture(c)).build();

    //Assert ---------------------------------------------------------------------------------
    assertThat(task2).extracting(MeshineryTask::getProcessorList).asList().hasSize(1);
    assertThat(task1).extracting(MeshineryTask::getProcessorList).asList().hasSize(1);
  }


}
