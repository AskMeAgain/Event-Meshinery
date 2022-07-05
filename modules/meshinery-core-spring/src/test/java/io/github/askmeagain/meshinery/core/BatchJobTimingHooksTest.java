package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.hooks.BatchJobTimingHooks;
import io.github.askmeagain.meshinery.core.hooks.CustomizeShutdownHook;
import io.github.askmeagain.meshinery.core.injecting.DataContextInjectApiController;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpyBean(BatchJobTimingHooks.class)
@MockBean(DataContextInjectApiController.class)
@SpringJUnitConfig(MeshineryAutoConfiguration.class)
@TestPropertySource(properties = "meshinery.core.batch-job=true")
class BatchJobTimingHooksTest extends AbstractCoreSpringTestBase {

  @Autowired
  List<CustomizeShutdownHook> hooks;

  @Autowired
  BatchJobTimingHooks batchJobTimingHooks;

  @Test
  void testSpringhook() {
    //Arrange ----------------------------------------------------------------------------------------------------------
    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(hooks).contains(batchJobTimingHooks);
  }
}
