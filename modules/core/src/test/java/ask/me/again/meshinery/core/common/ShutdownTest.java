package ask.me.again.meshinery.core.common;

import ask.me.again.meshinery.core.scheduler.RoundRobinScheduler;
import ask.me.again.meshinery.core.utils.AbstractTestBase;
import ask.me.again.meshinery.core.utils.context.TestContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShutdownTest extends AbstractTestBase {

  @SneakyThrows
  @Test
  @SuppressWarnings("unchecked")
  void shutdownTest() {
    //Arrange --------------------------------------------------------------------------------
    var flag = new AtomicBoolean(false);

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .registerShutdownHook(List.of(() -> flag.set(true)))
        .buildAndStart();

    Thread.sleep(1000);

    //Assert ---------------------------------------------------------------------------------
    assertThat(flag.get()).isTrue();
  }
}
