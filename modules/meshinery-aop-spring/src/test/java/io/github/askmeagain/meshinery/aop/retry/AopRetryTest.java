package io.github.askmeagain.meshinery.aop.retry;

import io.github.askmeagain.meshinery.aop.base.AopRetryTestService;
import io.github.askmeagain.meshinery.aop.base.E2eAopTestApplication;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.AbstractLogTestBase;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.sources.OutputCapture;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {
    E2eAopTestApplication.class,
    AopRetryTestService.class,
    AnnotationAwareAspectJAutoProxyCreator.class
})
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
    assertThatLogContainsMessage(output, "It worked!!! abc", "Retrying 2/6");
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
    connector.writeOutput("retryInEvent", context, new TaskData());
    var batchJobFinished = executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    assertThat(connector.getInputs(List.of("test-result")))
        .hasSize(1)
        .first()
        .extracting(TestContext::getIndex)
        .isEqualTo(1);
    assertThatLogContainsMessage(output, "It worked!!! abc", "Retrying 2/6");
  }
}