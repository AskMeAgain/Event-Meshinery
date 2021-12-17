package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.AbstractTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import io.github.askmeagain.meshinery.core.utils.sources.TestOutputSource;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShutdownTest extends AbstractTestBase {

  @SneakyThrows
  @Test
  @SuppressWarnings("unchecked")
  void shutdownHookTest() {
    //Arrange --------------------------------------------------------------------------------
    var inputSource = TestInputSource.<TestContext>builder()
        .todo(new TestContext(0))
        .build();
    var executor = Executors.newSingleThreadExecutor();
    var flag = new AtomicBoolean(false);
    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .outputSource(new TestOutputSource())
        .read(executor, "")
        .process(new TestContextProcessor(1))
        .write("")
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .isBatchJob(true)
        .task(task)
        .gracePeriodMilliseconds(0)
        .registerShutdownHook(List.of(scheduler -> flag.set(true)))
        .buildAndStart();

    Thread.sleep(2000);

    //Assert ---------------------------------------------------------------------------------
    assertThat(flag.get()).isTrue();
  }
}
