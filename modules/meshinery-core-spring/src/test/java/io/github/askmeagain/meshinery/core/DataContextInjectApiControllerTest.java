package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.task.TaskReplayFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(DataContextInjectApiController.class)
@ContextConfiguration(classes = {DataContextInjectApiController.class})
class DataContextInjectApiControllerTest {

  private static final MediaType APPLICATION_JSON_UTF8 =
      new MediaType(
          MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));


  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private TaskReplayFactory taskReplayFactory;

  @Test
  @SneakyThrows
  void injectContext() {
    //Arrange --------------------------------------------------------------------------------
    var objectMapper = new ObjectMapper();
    TestContext inputContext = new TestContext(0);
    TestContext result = new TestContext(1);
    Mockito.when(taskReplayFactory.injectDataAsync(eq("testTask"), eq(inputContext)))
        .thenReturn(CompletableFuture.completedFuture(result));

    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    mockMvc.perform(post("/inject/testTask")
            .contentType(APPLICATION_JSON_UTF8)
            .content(objectMapper.writeValueAsString(inputContext)))
        .andExpect(content().string("abc"))
        .andExpect(status()
            .isOk());

  }
}