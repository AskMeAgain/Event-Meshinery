package io.github.askmeagain.meshinery.connectors.mysql.e2e;

import io.github.askmeagain.meshinery.connectors.postgres.AbstractPostgresTestBase;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestApplication;
import io.github.askmeagain.meshinery.core.e2e.base.E2eTestBaseUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(classes = {E2eTestApplication.class, E2ePostgresTestConfiguration.class})
public class E2ePostgresTest extends AbstractPostgresTestBase {

  @Autowired
  ExecutorService executorService;

  @BeforeAll
  static void setupTest() {
    E2eTestBaseUtils.setupTest();
  }

  @Test
  @SneakyThrows
  void testE2ePostgres() {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    var batchJobFinished = executorService.awaitTermination(25_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    E2eTestBaseUtils.assertResultMap();
  }
}