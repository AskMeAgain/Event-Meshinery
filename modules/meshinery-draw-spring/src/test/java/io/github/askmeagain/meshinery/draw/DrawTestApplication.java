package io.github.askmeagain.meshinery.draw;

import io.github.askmeagain.meshinery.core.EnableMeshinery;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//@Disabled
class DrawTestApplication {

  @Test
  @SneakyThrows
  void test() {
    SpringApplication.run(TestApplication.class);

    Thread.sleep(100000);
  }

  @EnableMeshinery(injection = TestContext.class,
      connector = @EnableMeshinery.KeyDataContext(context = TestContext.class, key = String.class)
  )
  @EnableMeshineryDrawing
  @SpringBootApplication
  public static class TestApplication {

    @Bean
    MeshineryTask<String, TestContext> task1(MemoryConnector<String, TestContext> memoryConnector) {
      return MeshineryTaskFactory.<String, TestContext>builder()
          .connector(memoryConnector)
          .taskName("task1")
          .read(null, "0/10 * * * * *")
          .write("Output1")
          .build();
    }

    @Bean
    MeshineryTask<String, TestContext> task2(MemoryConnector<String, TestContext> memoryConnector) {
      return MeshineryTaskFactory.<String, TestContext>builder()
          .connector(memoryConnector)
          .taskName("task2")
          .read(null, "Output1")
          .write("Output2")
          .build();
    }

    @Bean
    MeshineryTask<String, TestContext> task3(MemoryConnector<String, TestContext> memoryConnector) {
      return MeshineryTaskFactory.<String, TestContext>builder()
          .connector(memoryConnector)
          .taskName("task3")
          .read(null, "Output2")
          .write("Output1")
          .build();
    }
  }
}
