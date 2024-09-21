package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.aop.common.EnableMeshineryAop;
import io.github.askmeagain.meshinery.aop.common.MeshineryAopTask;
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

@SpringBootTest(
    classes = {AopDisabledRetryTest.E2eAopTestApplication.class, AopDisabledRetryTest.AopRetryTestService.class})
@TestPropertySource(properties = {
    "meshinery.core.batch-job=true",
    "meshinery.aop.enabled=false",
    "meshinery.core.shutdown-on-finished=false",
    "meshinery.core.start-immediately=false"
})
public class AopDisabledRetryTest extends AbstractLogTestBase {

  @Autowired AopRetryTestService aopRetryTestService;
  @Autowired RoundRobinScheduler roundRobinScheduler;
  @Autowired ExecutorService executorService;
  @Autowired MeshinerySourceConnector<String, TestContext> connector;

  @Test
  @DirtiesContext
  void noRetry(OutputCapture output) throws InterruptedException {
    //Arrange --------------------------------------------------------------------------------
    var context = TestContext.builder()
        .id("abc")
        .build();

    //Act ------------------------------------------------------------------------------------
    roundRobinScheduler.start();
    var result = aopRetryTestService.run(context);
    var batchJobFinished = executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    assertThat(result.getIndex()).isEqualTo(context.getIndex() + 1);
    assertThat(connector.getInputs(List.of("test-result"))).isEmpty();
    assertThatLogContainsMessage(output, "Execute: " + Thread.currentThread().getName());
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

    @MeshineryAopTask(write = "retryEnd")
    public TestContext run(TestContext context) {
      log.info("Execute: {}", Thread.currentThread().getName());
      return context.toBuilder()
          .index(context.getIndex() + 1)
          .build();
    }
  }
}