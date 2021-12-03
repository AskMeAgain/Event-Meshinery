package io.github.askmeagain.meshinery.monitoring;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
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
}