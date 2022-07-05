package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.hooks.CustomizeShutdownHook;
import io.github.askmeagain.meshinery.core.injecting.DataContextInjectApiController;
import io.github.askmeagain.meshinery.core.setup.AbstractCoreSpringTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@MockBean(DataContextInjectApiController.class)
@SpringJUnitConfig(MeshineryAutoConfiguration.class)
@TestPropertySource(properties = "meshinery.core.shutdown-on-finished=true")
class EnabledShutdownHookTest extends AbstractCoreSpringTestBase {

  @Autowired
  CustomizeShutdownHook hook;

  @Test
  void testSpringhook() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(hook).isNotNull();
  }

}
