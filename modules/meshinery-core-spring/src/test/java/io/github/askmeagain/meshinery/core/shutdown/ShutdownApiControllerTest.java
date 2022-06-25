package io.github.askmeagain.meshinery.core.shutdown;

import io.github.askmeagain.meshinery.core.MeshineryAutoConfiguration;
import io.github.askmeagain.meshinery.core.injecting.DataContextInjectApiController;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.TaskReplayFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ShutdownApiController.class)
@ContextConfiguration(classes = MeshineryAutoConfiguration.class)
@TestPropertySource(properties = "meshinery.core.shutdown-api=true")
class ShutdownApiControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockBean
  RoundRobinScheduler roundRobinScheduler;

  @Test
  void testShutdown() throws Exception {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    mockMvc.perform(post("/shutdown")).andExpect(status().isOk());

    //Assert ---------------------------------------------------------------------------------
    Mockito.verify(roundRobinScheduler).gracefulShutdown();
  }

}