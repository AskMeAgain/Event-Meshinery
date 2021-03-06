package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.hooks.CustomizeShutdownHook;
import io.github.askmeagain.meshinery.core.injecting.DataContextInjectApiController;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.setup.AbstractCoreSpringTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@MockBean(DataContextInjectApiController.class)
@SpringJUnitConfig(MeshineryAutoConfiguration.class)
class DisabledShutdownHookTest  extends AbstractCoreSpringTestBase {

  @Autowired(required = false)
  CustomizeShutdownHook hook;

  @Autowired RoundRobinScheduler roundRobinScheduler;

  @Test
  void testSpringHook() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(hook).isNull();
  }
}
