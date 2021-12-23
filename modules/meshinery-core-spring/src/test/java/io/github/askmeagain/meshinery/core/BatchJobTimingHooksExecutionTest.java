package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.hooks.BatchJobTimingHooks;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

class BatchJobTimingHooksExecutionTest extends AbstractLogTestBase {

  @Test
  @SneakyThrows
  void testExecution() {
    //Arrange --------------------------------------------------------------------------------
    var applicationTimeHook = Mockito.spy(new BatchJobTimingHooks());
    var roundRobinScheduler = RoundRobinScheduler.builder()
        .registerStartupHook(List.of(applicationTimeHook))
        .registerShutdownHook(List.of(applicationTimeHook))
        .gracePeriodMilliseconds(1000)
        .buildAndStart();

    //Act ------------------------------------------------------------------------------------
    Thread.sleep(3000);
    roundRobinScheduler.gracefulShutdown();
    Thread.sleep(1500);
    //Assert ---------------------------------------------------------------------------------
    Mockito.verify(applicationTimeHook, Mockito.times(2)).accept(any());
    assertThatLogContainsMessage("Scheduler ran for 3 seconds", "Starting Batch job at");
  }

  @Override
  protected Class<?> loggerToUse() {
    return BatchJobTimingHooks.class;
  }
}
