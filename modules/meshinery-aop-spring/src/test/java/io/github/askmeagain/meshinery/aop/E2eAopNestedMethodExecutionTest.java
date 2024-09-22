package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.aop.common.EnableMeshineryAop;
import io.github.askmeagain.meshinery.aop.common.MeshineryAopTask;
import io.github.askmeagain.meshinery.aop.common.RetryMethod;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
    classes = {
        E2eAopNestedMethodExecutionTest.E2eAopTestApplication.class,
        E2eAopNestedMethodExecutionTest.E2eStep1Service.class,
        E2eAopNestedMethodExecutionTest.E2eStep2Service.class,
        E2eAopNestedMethodExecutionTest.E2eStep3Service.class,
        E2eAopNestedMethodExecutionTest.E2eStep4Service.class
    })
@TestPropertySource(properties = {
    "meshinery.core.batch-job=true",
    "meshinery.aop.enabled=true",
    "meshinery.core.shutdown-on-finished=false",
    "meshinery.core.start-immediately=false"
})
class E2eAopNestedMethodExecutionTest {

  @Autowired RoundRobinScheduler roundRobinScheduler;
  @Autowired ExecutorService executorService;
  @Autowired E2eStep1Service service;

  @Test
  @DirtiesContext
  @SneakyThrows
  void test() {
    //Arrange --------------------------------------------------------------------------------
    var context = TestContext.builder()
        .id("abc")
        .build();

    //Act ------------------------------------------------------------------------------------
    roundRobinScheduler.start();
    var result = service.step1(context);
    var batchJobFinished = executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(result)
        .returns(true, TestContext::isStep1)
        .returns(true, TestContext::isStep2)
        .returns(true, TestContext::isStep3)
        .returns(true, TestContext::isStep4);
    assertThat(batchJobFinished).isTrue();
  }

  @EnableMeshineryAop
  @TestConfiguration
  @EnableMeshinery(connector = {@EnableMeshinery.KeyDataContext(key = String.class, context = TestContext.class)})
  public static class E2eAopTestApplication {
    @Bean
    public ExecutorService executorService() {
      return Executors.newVirtualThreadPerTaskExecutor();
    }
  }

  @Slf4j
  @TestComponent
  @RequiredArgsConstructor
  public static class E2eStep1Service {
    private final E2eStep2Service e2EStep2Service;

    @MeshineryAopTask
    public TestContext step1(TestContext context) {
      var newContext = context.toBuilder()
          .step1(true)
          .build();

      return e2EStep2Service.step2(newContext);
    }
  }

  @Slf4j
  @TestComponent
  @RequiredArgsConstructor
  public static class E2eStep2Service {
    private final E2eStep3Service e2EStep3Service;
    private final AtomicBoolean atomicBoolean = new AtomicBoolean();

    @MeshineryAopTask(retryCount = 3, retryMethod = RetryMethod.EVENT)
    public TestContext step2(TestContext context) {
      log.error("Step2");
      if (!atomicBoolean.get()) {
        atomicBoolean.set(true);
        throw new RuntimeException("Arrg");
      }
      var newContext = context.toBuilder()
          .step2(true)
          .build();
      return e2EStep3Service.step3(newContext);
    }
  }

  @Slf4j
  @TestComponent
  @RequiredArgsConstructor
  public static class E2eStep3Service {
    private final E2eStep4Service e2EStep4Service;
    private final AtomicBoolean atomicBoolean = new AtomicBoolean();

    @MeshineryAopTask(retryCount = 3, retryMethod = RetryMethod.MEMORY)
    public TestContext step3(TestContext context) {
      log.error("Step3");
      if (!atomicBoolean.get()) {
        atomicBoolean.set(true);
        throw new RuntimeException("Arrg");
      }
      var newContext = context.toBuilder()
          .step3(true)
          .build();
      return e2EStep4Service.step4(newContext);
    }
  }

  @Slf4j
  @TestComponent
  public static class E2eStep4Service {
    private final AtomicBoolean atomicBoolean = new AtomicBoolean();

    @MeshineryAopTask(retryCount = 3, retryMethod = RetryMethod.MEMORY)
    public TestContext step4(TestContext context) {
      log.error("Step4");
      if (!atomicBoolean.get()) {
        atomicBoolean.set(true);
        throw new RuntimeException("Arrg");
      }
      return context.toBuilder()
          .step4(true)
          .build();
    }
  }
}