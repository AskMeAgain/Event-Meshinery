package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "meshinery.core.shutdown-on-finished=false")
public abstract class AbstractCoreSpringTestBase {

  @Autowired
  protected RoundRobinScheduler roundRobinScheduler;

  @AfterEach
  void shutdown() {
    roundRobinScheduler.gracefulShutdown();
  }
}
