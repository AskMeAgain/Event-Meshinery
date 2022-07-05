package io.github.askmeagain.meshinery.core.injecting;

import io.github.askmeagain.meshinery.core.AbstractCoreSpringTestBase;
import io.github.askmeagain.meshinery.core.MeshineryAutoConfiguration;
import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.TaskReplayFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(DataContextInjectApiController.class)
@ContextConfiguration(classes = MeshineryAutoConfiguration.class)
@TestPropertySource(properties = "meshinery.core.inject=io.github.askmeagain.meshinery.core.utils.context.TestContext")
class DataContextInjectApiControllerTest extends AbstractCoreSpringTestBase {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TestContext INPUT_CONTEXT = new TestContext(0);
  private static final TestContext RESULT_CONTEXT = new TestContext(1);
  private static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
      MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(),
      StandardCharsets.UTF_8
  );

  @Autowired private DataContextInjectApiController controller;
  @Autowired private MockMvc mockMvc;
  @Autowired private RoundRobinScheduler roundRobinScheduler;

  @MockBean private TaskReplayFactory taskReplayFactory;

  @BeforeEach
  void fillingPostConstructMethod() {
    controller.setup();
  }

  @Test
  @SneakyThrows
  void injectContext() {
    //Arrange --------------------------------------------------------------------------------
    Mockito.when(taskReplayFactory.injectData(eq("testTask"), eq(INPUT_CONTEXT)))
        .thenReturn(RESULT_CONTEXT);

    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    mockMvc.perform(post("/inject/TestContext/testTask")
            .contentType(APPLICATION_JSON_UTF8)
            .content(OBJECT_MAPPER.writeValueAsString(INPUT_CONTEXT)))
        .andExpect(content().string(OBJECT_MAPPER.writeValueAsString(RESULT_CONTEXT)))
        .andExpect(status()
            .isOk());

    roundRobinScheduler.gracefulShutdown();
  }

  @Test
  @SneakyThrows
  void injectContextAsync() {
    //Arrange --------------------------------------------------------------------------------
    Mockito.when(taskReplayFactory.injectDataAsync(eq("testTask"), eq(INPUT_CONTEXT)))
        .thenReturn(null);

    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    mockMvc.perform(post("/inject/TestContext/testTask/async")
            .contentType(APPLICATION_JSON_UTF8)
            .content(OBJECT_MAPPER.writeValueAsString(INPUT_CONTEXT)))
        .andExpect(content().string("Accepted"))
        .andExpect(status()
            .isAccepted());

    roundRobinScheduler.gracefulShutdown();
  }
}