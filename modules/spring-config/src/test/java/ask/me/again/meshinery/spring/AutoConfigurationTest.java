package ask.me.again.meshinery.spring;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
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
