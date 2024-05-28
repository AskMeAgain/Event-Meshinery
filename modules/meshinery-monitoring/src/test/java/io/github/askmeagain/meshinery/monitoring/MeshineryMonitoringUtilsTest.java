package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.processor.TestContextProcessor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MeshineryMonitoringUtilsTest {

  @Test
  void testLambdaProcessorName() {
    //Arrange --------------------------------------------------------------------------------
    MeshineryProcessor<TestContext, TestContext> processor = c -> c;

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
}