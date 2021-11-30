package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@EnableMeshinery
@SpringJUnitConfig
class AutoConfigurationTest {

  @Autowired
  RoundRobinScheduler scheduler;

  @Test
  void autoConfigTest() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(scheduler).isNotNull();
    scheduler.gracefulShutdown();
  }

}
