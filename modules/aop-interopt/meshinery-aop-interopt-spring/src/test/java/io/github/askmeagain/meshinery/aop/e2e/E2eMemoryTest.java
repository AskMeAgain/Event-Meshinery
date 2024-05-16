package io.github.askmeagain.meshinery.aop.e2e;

import io.github.askmeagain.meshinery.aop.e2e.base.E2eAopTestApplication;
import io.github.askmeagain.meshinery.aop.e2e.base.E2eTestService;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@Slf4j
@SpringBootTest(classes = {E2eAopTestApplication.class})
@TestPropertySource(properties = {
    "meshinery.core.batch-job=true",
    "meshinery.aop.enabled=true",
    "meshinery.core.shutdown-on-finished=false"
})
class E2eMemoryTest {

  //  @Autowired
  //  ExecutorService executorService;
  @Autowired
  E2eTestService service;

  @Test
  @SneakyThrows
  void test() {
    //Arrange --------------------------------------------------------------------------------
    var context = TestContext.builder()
        .id("abc")
        .build();

    //Act ------------------------------------------------------------------------------------
    service.executeViaJob(context);
    //var batchJobFinished = executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    //assertThat(batchJobFinished).isTrue();
  }
}

