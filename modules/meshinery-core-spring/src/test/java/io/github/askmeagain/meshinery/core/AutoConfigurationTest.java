package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.injecting.DataContextInjectApiController;
import io.github.askmeagain.meshinery.core.scheduler.MeshineryCoreProperties;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@MockBean(DataContextInjectApiController.class)
@SpringBootTest(classes = MeshineryAutoConfiguration.class)
@TestPropertySource(properties = {"meshinery.core.inject=abc", "meshinery.core.shutdown-on-finished=false"})
class AutoConfigurationTest {


  @Autowired
  RoundRobinScheduler roundRobinScheduler;

  @Test
  void autoConfigTest() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(roundRobinScheduler).isNotNull();
  }

  @Test
  void autoConfigTest(@Autowired MeshineryCoreProperties properties) {
    //Arrange ----------------------------------------------------------------------------------------------------------
    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(properties.getInject()).contains("abc");
  }

  @AfterEach
  void shutdown() {
    roundRobinScheduler.gracefulShutdown();
  }

}
