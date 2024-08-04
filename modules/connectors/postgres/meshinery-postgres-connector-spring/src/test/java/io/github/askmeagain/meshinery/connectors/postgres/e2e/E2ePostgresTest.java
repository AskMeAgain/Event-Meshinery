package io.github.askmeagain.meshinery.connectors.postgres.e2e;

import io.github.askmeagain.meshinery.connectors.postgres.AbstractSpringPostgresTestBase;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestBaseUtils;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DirtiesContext
@SpringBootTest(classes = {E2eTestApplication.class, E2ePostgresTestConfiguration.class})
@TestPropertySource(properties = {
    "meshinery.core.batch-job=true",
    "meshinery.core.shutdown-on-finished=false",
    "meshinery.core.grace-period-milliseconds=5000",
    "meshinery.core.backpressure-limit=150",
    "meshinery.core.start-immediately=false"
})
public class E2ePostgresTest extends AbstractSpringPostgresTestBase {

  @Autowired
  ExecutorService executorService;
  @Autowired RoundRobinScheduler roundRobinScheduler;

  @BeforeEach
  void setupTest() {
    E2eTestBaseUtils.setupTest();
  }

  @Test
  @SneakyThrows
  void testE2ePostgres() {
    //Arrange --------------------------------------------------------------------------------
    roundRobinScheduler.start();

    //Act ------------------------------------------------------------------------------------
    var batchJobFinished = executorService.awaitTermination(160_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    E2eTestBaseUtils.assertResultMap();
  }
}