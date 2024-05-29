package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.common.InputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.injecting.DataContextInjectApiController;
import io.github.askmeagain.meshinery.core.setup.AbstractCoreSpringTestBase;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.task.TaskData;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.decorators.TestInputSourceDecoratorFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@MockBean(DataContextInjectApiController.class)
@SpringBootTest(classes = {MeshineryAutoConfiguration.class, DecoratorBeansTest.TestDecoratorConfiguration.class})
class DecoratorBeansTest extends AbstractCoreSpringTestBase {

  @Autowired
  InputSourceDecoratorFactory decorator;
  @SpyBean
  MemoryConnector<String, TestContext> connector;

  @Test
  void autoConfigTest() throws InterruptedException {
    //Arrange ----------------------------------------------------------------------------------------------------------
    connector.writeOutput("Abc", TestContext.builder()
        .id("a")
        .build(), new TaskData());

    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(roundRobinScheduler).isNotNull();

    Thread.sleep(3000);

    Mockito.verify(decorator, Mockito.atLeastOnce()).decorate(any());
    Mockito.verify(connector, Mockito.atLeastOnce()).getInputs(any());
    Mockito.verify(connector, Mockito.atLeast(2)).writeOutput(any(), any(), any());
  }

  @TestConfiguration
  public static class TestDecoratorConfiguration {

    @Bean
    public MemoryConnector<String, TestContext> memoryConnector() {
      return new MemoryConnector<>();
    }

    @Bean
    public InputSourceDecoratorFactory connectorDecoratorFactory() {
      return Mockito.spy(new TestInputSourceDecoratorFactory(new AtomicInteger()));
    }

    @Bean
    public MeshineryTask<String, TestContext> task(MemoryConnector<String, TestContext> connector) {
      return MeshineryTaskFactory.<String, TestContext>builder()
          .read("Abc")
          .connector(connector)
          .write("Def")
          .build();
    }
  }
}
