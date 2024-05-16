package io.github.askmeagain.meshinery.aop.e2e;

import io.github.askmeagain.meshinery.aop.e2e.base.E2eAopTestApplication;
import io.github.askmeagain.meshinery.aop.e2e.base.E2eTestService;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(classes = {E2eAopTestApplication.class})
@TestPropertySource(properties = {
    "meshinery.core.batch-job=true",
    "meshinery.aop.enabled=true",
    "meshinery.core.shutdown-on-finished=false"
})
class E2eAopTest {

  @Autowired ExecutorService executorService;
  @Autowired E2eTestService service;
  @Autowired MeshineryConnector<String, DataContext> connector;

  @Test
  @SneakyThrows
  void test() {
    //Arrange --------------------------------------------------------------------------------
    var context = TestContext.builder()
        .id("abc")
        .build();

    //Act ------------------------------------------------------------------------------------
    service.executeViaJob(context);
    var batchJobFinished = executorService.awaitTermination(10_000, TimeUnit.MILLISECONDS);

    //Assert ---------------------------------------------------------------------------------
    assertThat(connector.getInputs(List.of("test"))).isEmpty();
    assertThat(batchJobFinished).isTrue();
  }
}

