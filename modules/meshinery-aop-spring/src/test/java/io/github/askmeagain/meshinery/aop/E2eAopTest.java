package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.aop.common.EnableMeshineryAop;
import io.github.askmeagain.meshinery.aop.common.MeshineryAopTask;
import io.github.askmeagain.meshinery.aop.common.RetryMethod;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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

@SpringBootTest(classes = {E2eAopTest.E2eAopTestApplication.class, E2eAopTest.E2eTestService.class})
@TestPropertySource(properties = {
    "meshinery.core.batch-job=true",
    "meshinery.aop.enabled=true",
    "meshinery.core.shutdown-on-finished=false",
    "meshinery.core.start-immediately=false"
})
class E2eAopTest {

  @Autowired RoundRobinScheduler roundRobinScheduler;
  @Autowired ExecutorService executorService;
  @Autowired E2eTestService service;
  @Autowired MeshinerySourceConnector<String, ? extends MeshineryDataContext> connector;

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
    service.executeViaJob(context);
    var batchJobFinished = executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(connector.getInputs(List.of("test"))).isEmpty();
    assertThat(batchJobFinished).isTrue();
  }

  @Test
  @DirtiesContext
  @SneakyThrows
  void retryTest() {
    //Arrange --------------------------------------------------------------------------------
    var context = TestContext.builder()
        .id("abc")
        .build();

    //Act ------------------------------------------------------------------------------------
    roundRobinScheduler.start();
    service.retry3TimesTest(context);
    var batchJobFinished = executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(connector.getInputs(List.of("test"))).isEmpty();
    assertThat(batchJobFinished).isTrue();
  }

  @EnableMeshineryAop
  @TestConfiguration
  @EnableMeshinery(connector = {@EnableMeshinery.KeyDataContext(key = String.class, context = TestContext.class)})
  public static class E2eAopTestApplication {
    @Bean
    public ExecutorService executorService() {
      return Executors.newFixedThreadPool(1);
    }
  }

  @Slf4j
  @TestComponent
  public static class E2eTestService {

    private final Map<String, AtomicInteger> map = new ConcurrentHashMap<>();

    @MeshineryAopTask
    public void executeViaJob(TestContext context) {
      log.info("executed inside job? " + context.getId());
    }

    @MeshineryAopTask(retryCount = 3, retryMethod = RetryMethod.MEMORY)
    public TestContext retry3TimesTest(TestContext context) {
      map.computeIfAbsent(context.getId(), k -> new AtomicInteger(0));
      var result = map.get(context.getId()).incrementAndGet();

      if (result <= 3) {
        throw new RuntimeException("please retry");
      }

      log.info("received task3: {}", context.getId());
      return context;
    }
  }
}