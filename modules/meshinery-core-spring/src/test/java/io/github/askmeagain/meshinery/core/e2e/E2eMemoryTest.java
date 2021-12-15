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
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@Slf4j
@SpringJUnitConfig(
    classes = {E2eTestApplication.class, MemoryTestConfiguration.class},
    initializers = ConfigDataApplicationContextInitializer.class
)
class E2eMemoryTest {

  @Autowired
  ExecutorService executorService;

  @BeforeAll
  static void createTopics() {
    E2eTestBaseUtils.setupTest();
  }

  @Test
  @SneakyThrows
  void test() {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    E2eTestBaseUtils.assertResultMap();
  }
}

