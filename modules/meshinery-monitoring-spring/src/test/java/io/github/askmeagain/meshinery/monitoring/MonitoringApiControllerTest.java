package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import io.github.askmeagain.meshinery.core.utils.sources.TestOutputSource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MonitoringApiController.class)
@ContextConfiguration(classes = {MonitoringApiController.class})
class MonitoringApiControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @SneakyThrows
  void prometheus() {
    mockMvc.perform(get("/metrics/prometheus"))
        .andExpect(status()
            .isOk())
        .andExpect(content()
            .string(containsString("request_time")))
        .andExpect(content()
            .string(containsString("request_time")))
        .andExpect(content()
            .string(containsString("processing_counter")))
        .andExpect(content()
            .string(containsString("processing_counter")));
  }

  @Test
  @SneakyThrows
  void test() {
    //Arrange --------------------------------------------------------------------------------
    MDC.put("", "");
    var config = new MeshineryMonitoringAutoConfiguration();
    var executor = Executors.newFixedThreadPool(3);

    RoundRobinScheduler.builder()
        .registerStartupHook(List.of(config.executorRegistration()))
        .task(MeshineryTaskFactory.<String, TestContext>builder()
            .taskName("cool-task-name")
            .read(executor, "test")
            .inputSource(new TestInputSource(List.of(TestContext.builder().build()), 1, 0, 0))
            .process((c, e) -> CompletableFuture.supplyAsync(() -> {
              wait3Sec();
              return c;
            }, e))
            .outputSource(new TestOutputSource())
            .build())
        .task(MeshineryTaskFactory.<String, TestContext>builder()
            .taskName("cool-task-name-2")
            .read(executor, "test2")
            .inputSource(new TestInputSource(List.of(TestContext.builder().build()), 1, 0, 0))
            .process((c, e) -> CompletableFuture.supplyAsync(() -> {
              wait3Sec();
              return c;
            }, e))
            .outputSource(new TestOutputSource())
            .build())
        .isBatchJob(false)
        .buildAndStart();

    Thread.sleep(1500);

    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    mockMvc.perform(get("/metrics/prometheus"))
        .andExpect(status()
            .isOk())
        .andExpect(content()
            .string(containsString("executor_max_threads{executor=\"test-executor\",} 3.0")))
        .andExpect(content()
            .string(containsString("executor_max_threads{executor=\"input-executor\",} 1.0")))
        .andExpect(content()
            .string(containsString("executor_max_threads{executor=\"output-executor\",} 1.0")))
        .andExpect(content()
            .string(containsString("executor_active_threads{executor=\"test-executor\",} 2.0")))
        .andExpect(content()
            .string(containsString("executor_active_threads{executor=\"input-executor\",} 1.0")))
        .andExpect(content()
            .string(containsString("executor_active_threads{executor=\"output-executor\",} 1.0")));
  }

  @SneakyThrows
  private void wait3Sec() {
    Thread.sleep(3000);
  }
}