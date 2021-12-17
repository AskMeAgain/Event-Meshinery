package io.github.askmeagain.meshinery.core.e2e;

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
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(classes = {E2eTestApplication.class, MemoryTestConfiguration.class})
@TestPropertySource(properties = "meshinery.core.batch-job=true")
class E2eMemoryTest {

  @Autowired
  ExecutorService executorService;

  @BeforeAll
  static void setupTest() {
    E2eTestBaseUtils.setupTest();
  }

  @Test
  @SneakyThrows
  void test() {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    var batchJobFinished = executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(batchJobFinished).isTrue();
    E2eTestBaseUtils.assertResultMap();
  }
}

