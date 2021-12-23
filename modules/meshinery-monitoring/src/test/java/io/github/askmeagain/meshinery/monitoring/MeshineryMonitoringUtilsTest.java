package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = MeshineryMonitoringUtilsTest.ProxyBeanTestProcessor.class)
class MeshineryMonitoringUtilsTest {

  @Test
  void testLambdaProcessorName() {
    //Arrange --------------------------------------------------------------------------------
    MeshineryProcessor<TestContext, TestContext> processor = (c, e) -> CompletableFuture.completedFuture(c);

    //Act ------------------------------------------------------------------------------------
    var result = MeshineryMonitoringUtils.convertLambdaProcessorName(processor.getClass());

    //Assert ---------------------------------------------------------------------------------
    assertThat(result).contains("lambda-");
  }

  @Test
  void testSimpleProcessorName() {
    //Arrange --------------------------------------------------------------------------------
    var processor = new TestContextProcessor(0);

    //Act ------------------------------------------------------------------------------------
    var result = MeshineryMonitoringUtils.convertLambdaProcessorName(processor.getClass());

    //Assert ---------------------------------------------------------------------------------
    assertThat(result).contains("TestContextProcessor");
  }

  @Test
  void testProxyProcessorName(@Autowired ProxyBeanTestProcessor testContextProcessor) {
    //Arrange --------------------------------------------------------------------------------
    //Act ------------------------------------------------------------------------------------
    var result = MeshineryMonitoringUtils.convertLambdaProcessorName(testContextProcessor.getClass());

    //Assert ---------------------------------------------------------------------------------
    assertThat(result).contains("ProxyBeanTestProcessor");
    assertThat(testContextProcessor.getClass().getSimpleName()).contains("CGLIB");
  }

  @Component
  @Scope(value="prototype", proxyMode= ScopedProxyMode.TARGET_CLASS)
  public static class ProxyBeanTestProcessor extends TestContextProcessor {
    public ProxyBeanTestProcessor() {
      super(0);
    }
  }
}