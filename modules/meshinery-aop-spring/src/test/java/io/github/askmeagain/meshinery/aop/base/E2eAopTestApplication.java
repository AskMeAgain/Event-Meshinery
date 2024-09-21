package io.github.askmeagain.meshinery.aop.base;

import io.github.askmeagain.meshinery.aop.common.EnableMeshineryAop;
import io.github.askmeagain.meshinery.core.EnableMeshinery;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@EnableMeshineryAop
@TestConfiguration
@EnableMeshinery(connector = {@EnableMeshinery.KeyDataContext(key = String.class, context = TestContext.class)})
public class E2eAopTestApplication {

  @Bean
  public ExecutorService executorService() {
    return Executors.newFixedThreadPool(1);
  }

}