package io.github.askmeagain.meshinery.core.common;

import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.source.StaticInputSource;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.AbstractTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import io.github.askmeagain.meshinery.core.utils.sources.TestInputSource;
import io.github.askmeagain.meshinery.core.utils.sources.TestOutputSource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

class ShutdownTest extends AbstractTestBase {

  @SneakyThrows
  @Test
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
        .read("")
        .process(new TestContextProcessor(1))
        .write("")
        .build();

    //Act ------------------------------------------------------------------------------------
    RoundRobinScheduler.<String, TestContext>builder()
        .batchJob(true)
        .task(task)
        .executorService(executor)
        .gracePeriodMilliseconds(0)
        .registerShutdownHook(List.of(scheduler -> flag.set(true)))
        .build()
        .start();

    Thread.sleep(2000);

    //Assert ---------------------------------------------------------------------------------
    assertThat(flag.get()).isTrue();
  }

  @Test
  @SneakyThrows
  void gracefulShutdownTest() {
    //Arrange --------------------------------------------------------------------------------
    var counter = new AtomicInteger();

    var inputSource = new StaticInputSource<String, TestContext>(
        "StaticInputSource",
        keys -> {
          if (counter.get() > 120) {
            return Collections.emptyList();
          } else {
            counter.incrementAndGet();
            return List.of(TestContext.builder().build());
          }
        }
    );
    var executor = Executors.newFixedThreadPool(5);
    var outputSource = Mockito.spy(new TestOutputSource());
    var task = MeshineryTaskFactory.<String, TestContext>builder()
        .inputSource(inputSource)
        .outputSource(outputSource)
        .read("")
        .process(new TestContextProcessor(1))
        .write("")
        .build();

    //Act ------------------------------------------------------------------------------------
    var scheduler = RoundRobinScheduler.<String, TestContext>builder()
        .batchJob(false)
        .task(task)
        .executorService(executor)
        .gracePeriodMilliseconds(0)
        .build()
        .start();

    Thread.sleep(1300);

    scheduler.gracefulShutdown();
    var isShutdown = executor.awaitTermination(35000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(isShutdown).isTrue();
    Mockito.verify(outputSource, times(counter.get())).writeOutput(any(), any(), any());
  }
}