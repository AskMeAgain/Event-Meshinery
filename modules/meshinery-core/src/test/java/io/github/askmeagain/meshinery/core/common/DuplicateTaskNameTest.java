package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskVerifier;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import io.github.askmeagain.meshinery.core.utils.sources.TestOutputSource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuplicateTaskNameTest {

  @Test
  void duplicateName() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    var duplicateTask = MeshineryTaskFactory.<String, TestContext>builder()
        .taskName("duplicateTask")
        .inputSource(new TestInputSource(Collections.emptyList(), 0, 0, 0))
        .read(Executors.newSingleThreadExecutor(), "")
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThatThrownBy(() -> MeshineryTaskVerifier.verifyTasks(List.of(duplicateTask, duplicateTask)))
        .isInstanceOf(RuntimeException.class)
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
        .read(null, "abc")
        .outputSource(outputSource)
        .build();
    var duplicateTask2 = MeshineryTaskFactory.<String, TestContext>builder()
        .taskName("task2")
        .inputSource(inputSource)
        .outputSource(outputSource)
        .read(null, "abc")
        .build();

    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThatThrownBy(() -> MeshineryTaskVerifier.verifyTasks(List.of(duplicateTask1, duplicateTask2)))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Found duplicate Read keys: [abc]");
  }

}
