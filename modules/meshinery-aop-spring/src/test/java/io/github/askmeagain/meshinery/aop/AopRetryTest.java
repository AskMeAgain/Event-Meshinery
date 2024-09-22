package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.aop.common.EnableMeshineryAop;
import io.github.askmeagain.meshinery.aop.common.MeshineryAopTask;
import io.github.askmeagain.meshinery.aop.common.RetryMethod;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.OutputCapture;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {AopRetryTest.E2eAopTestApplication.class, AopRetryTest.AopRetryTestService.class})
@TestPropertySource(properties = {
    "meshinery.core.batch-job=true",
    "meshinery.aop.enabled=true",
    "meshinery.core.shutdown-on-finished=false",
    "meshinery.core.start-immediately=false"
})
public class AopRetryTest extends AbstractLogTestBase {

  @Autowired AopRetryTestService aopRetryTestService;
  @Autowired RoundRobinScheduler roundRobinScheduler;
  @Autowired ExecutorService executorService;
  @Autowired MeshinerySourceConnector<String, TestContext> connector;

  @Test
  @DirtiesContext
  void InMemoryRetry(OutputCapture output) throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var context = TestContext.builder()
        .id("abc")
        .build();

    //Act ------------------------------------------------------------------------------------
    roundRobinScheduler.start();
    aopRetryTestService.retryInMemory(context);
    var batchJobFinished = executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    assertThat(connector.getInputs(List.of("test-result")))
        .hasSize(1)
        .first()
        .extracting(TestContext::getIndex)
        .isEqualTo(1);
    assertThatLogContainsMessage(output, "It worked!!! abc on thread virtual-", "Retrying 2/6");
  }

  @Test
  @DirtiesContext
  void InEventRetry(OutputCapture output) throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var context = TestContext.builder()
        .id("abc")
        .build();

    //Act ------------------------------------------------------------------------------------
    roundRobinScheduler.start();
    aopRetryTestService.retryInEvent(context);
    var batchJobFinished = executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    assertThat(connector.getInputs(List.of("test-result")))
        .hasSize(1)
        .first()
        .extracting(TestContext::getIndex)
        .isEqualTo(1);
    assertThatLogContainsMessage(output, "It worked!!! abc on thread virtual-", "Retrying 2/6");
  }

  @EnableMeshineryAop
  @TestConfiguration
  @EnableMeshinery(connector = {@EnableMeshinery.KeyDataContext(key = String.class, context = TestContext.class)})
  public static class E2eAopTestApplication {
    @Bean
    public ExecutorService executorService() {
      var factory = Thread.ofVirtual().name("virtual-", 0).factory();
      return Executors.newThreadPerTaskExecutor(factory);
    }
  }

  @Slf4j
  @TestComponent
  public static class AopRetryTestService {

    private final AtomicInteger flag = new AtomicInteger();

    @MeshineryAopTask(write = "retryEnd", retryCount = 6, retryMethod = RetryMethod.EVENT)
    public TestContext retryInEvent(TestContext context) {
      log.info("starting with retry now: {}", context.getId());
      if (flag.incrementAndGet() < 3) {
        log.error("error in between");
        throw new RuntimeException("err");
      }
      return context.toBuilder()
          .index(context.getIndex() + 1)
          .build();
    }

    @MeshineryAopTask(write = "retryEnd", retryCount = 6, retryMethod = RetryMethod.MEMORY)
    public TestContext retryInMemory(TestContext context) {
      log.info("starting with retry now: {}", context.getId());
      if (flag.incrementAndGet() < 3) {
        log.error("error in between");
        throw new RuntimeException("err");
      }
      return context.toBuilder()
          .index(context.getIndex() + 1)
          .build();
    }

    @MeshineryAopTask(write = "test-result")
    public TestContext retryEnd(TestContext context) {
      log.info("It worked!!! {} on thread {}", context.getId(), Thread.currentThread().getName());
      return context;
    }
  }
}