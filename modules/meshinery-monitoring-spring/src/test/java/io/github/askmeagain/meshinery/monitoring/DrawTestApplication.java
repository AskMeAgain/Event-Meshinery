package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.EnableMeshinery;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.monitoring.apis.DrawerApiController;
import io.github.askmeagain.meshinery.monitoring.config.MeshineryDrawerConfiguration;
import io.github.askmeagain.meshinery.monitoring.decorators.InputSourceTimingDecoratorFactory;
import io.github.askmeagain.meshinery.monitoring.decorators.ProcessorTimingDecorator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@WebMvcTest(DrawerApiController.class)
@MockBean({InputSourceTimingDecoratorFactory.class, ProcessorTimingDecorator.class})
@ContextConfiguration(classes = {MeshineryDrawerConfiguration.class, DrawTestApplication.TestApplication.class})
class DrawTestApplication {

  @Autowired
  MockMvc mockMvc;
  @Autowired
  RoundRobinScheduler roundRobinScheduler;

  @Test
  @SneakyThrows
  void test() {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    mockMvc.perform(get("/draw/mermaid"))
        .andExpect(status()
            .isOk())
        .andExpect(content().string(containsString("graph LR")))
        .andExpect(content().string(containsString("task1 --> task2")))
        .andExpect(content().string(containsString("task2 --> task3")))
        .andExpect(content().string(containsString("task3 --> task2")));
  }

  @EnableMeshinery(
      injection = TestContext.class,
      connector = @EnableMeshinery.KeyDataContext(context = TestContext.class, key = String.class)
  )
  @SpringBootApplication
  public static class TestApplication {

    @Bean
    public ExecutorService executorService() {
      return Executors.newSingleThreadExecutor();
    }

    @Bean
    MeshineryTask<String, TestContext> task1(MemoryConnector<String, TestContext> memoryConnector) {
      return MeshineryTask.<String, TestContext>builder()
          .connector(memoryConnector)
          .taskName("task1")
          .read("0/10 * * * * *")
          .write("Output1")
          .build();
    }

    @Bean
    MeshineryTask<String, TestContext> task2(MemoryConnector<String, TestContext> memoryConnector) {
      return MeshineryTask.<String, TestContext>builder()
          .connector(memoryConnector)
          .taskName("task2")
          .read("Output1")
          .write("Output2")
          .build();
    }

    @Bean
    MeshineryTask<String, TestContext> task3(MemoryConnector<String, TestContext> memoryConnector) {
      return MeshineryTask.<String, TestContext>builder()
          .connector(memoryConnector)
          .taskName("task3")
          .read("Output2")
          .write("Output1")
          .build();
    }
  }
}
