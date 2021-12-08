package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@MockBean(DataContextInjectApiController.class)
@SpringBootTest(classes = MeshineryAutoConfiguration.class)
@TestPropertySource(properties = "meshinery.core.inject=abc")
class AutoConfigurationTest {

  @Test
  void autoConfigTest(@Autowired RoundRobinScheduler scheduler) {
    //Arrange ----------------------------------------------------------------------------------------------------------
    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(scheduler).isNotNull();
    scheduler.gracefulShutdown();
  }

  @Test
  void autoConfigTest(@Autowired MeshineryConfigProperties properties) {
    //Arrange ----------------------------------------------------------------------------------------------------------
    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(properties.getInject()).contains("abc");
  }

}
