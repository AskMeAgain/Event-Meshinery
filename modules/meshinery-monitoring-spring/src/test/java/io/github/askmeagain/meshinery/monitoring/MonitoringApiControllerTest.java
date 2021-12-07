package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.List;
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
    RoundRobinScheduler.builder()
        .registerStartupHook(List.of(config.executorRegistration()))
        .isBatchJob(false)
        .buildAndStart();

    //Act ------------------------------------------------------------------------------------
    //Assert ---------------------------------------------------------------------------------
    mockMvc.perform(get("/metrics/prometheus"))
        .andExpect(status()
            .isOk())
        .andExpect(content()
            .string(containsString("executor_active_threads")))
        .andExpect(content()
            .string(containsString("executor=\"input-executor\"")))
        .andExpect(content()
            .string(containsString("executor=\"output-executor\"")));
  }
}