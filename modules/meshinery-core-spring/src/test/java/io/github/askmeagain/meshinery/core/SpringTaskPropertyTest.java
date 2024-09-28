package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.injecting.DataContextInjectApiController;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.setup.AbstractCoreSpringTestBase;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.TASK_IGNORE_NO_KEYS_WARNING;
import static org.assertj.core.api.Assertions.assertThat;

@MockBean(DataContextInjectApiController.class)
@SpringBootTest(classes = MeshineryAutoConfiguration.class)
@TestPropertySource(properties = {
    "meshinery.core.task-properties.test-task.abc=abc",
    "meshinery.core.task-properties.test-task.abc2=abc2",
    "meshinery.core.task-properties.test-task." + TASK_IGNORE_NO_KEYS_WARNING + "=true",
    "meshinery.core.task-properties.test-task-2.def=def",
    "meshinery.core.task-properties.test-task-2.abc2=abc",
    "meshinery.core.task-properties.test-task-2." + TASK_IGNORE_NO_KEYS_WARNING + "=true",
})
class SpringTaskPropertyTest extends AbstractCoreSpringTestBase {

  @Test
  void springPropertyTest(@Autowired MeshineryCoreProperties properties) {
    //Arrange ----------------------------------------------------------------------------------------------------------
    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(properties.getTaskProperties().get("test-task").get("abc")).isEqualTo("abc");
    assertThat(properties.getTaskProperties().get("test-task").get("abc2")).isEqualTo("abc2");

    assertThat(properties.getTaskProperties().get("test-task-2").get("def")).isEqualTo("def");
    assertThat(properties.getTaskProperties().get("test-task-2").get("abc2")).isEqualTo("abc");
  }

  @Test
  void sprintPropertyTaskInjection(@Autowired MeshineryCoreProperties properties) {
    //Arrange --------------------------------------------------------------------------------
    var meshineryConnector = new MemoryConnector<String, TestContext>();

    var testTask1 = MeshineryTask.<String, TestContext>builder()
        .taskName("test-task")
        .inputSource(meshineryConnector)
        .putData("key-1", "nicevalue1")
        .build();

    var testTask2 = MeshineryTask.<String, TestContext>builder()
        .inputSource(meshineryConnector)
        .taskName("test-task-2")
        .putData("key-2", "nicevalue2")
        .build();

    //dirty cast
    var taskList = List.<MeshineryTask<?, ?>>of(testTask1, testTask2);

    //Act ------------------------------------------------------------------------------------
    var result = MeshineryUtils.injectProperties(taskList, properties);

    //Assert ---------------------------------------------------------------------------------
    assertThat(result.get(0).getTaskData().getProperties())
        .containsEntry("key-1", List.of("nicevalue1"))
        .containsEntry("abc", List.of("abc"))
        .containsEntry("abc2", List.of("abc2"));

    assertThat(result.get(1).getTaskData().getProperties())
        .containsEntry("key-2", List.of("nicevalue2"))
        .containsEntry("abc2", List.of("abc"))
        .containsEntry("def", List.of("def"));
  }
}