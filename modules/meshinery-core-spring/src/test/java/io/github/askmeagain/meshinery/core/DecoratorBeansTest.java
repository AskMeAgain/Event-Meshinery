package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.common.ConnectorDecoratorFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.injecting.DataContextInjectApiController;
import io.github.askmeagain.meshinery.core.scheduler.RoundRobinScheduler;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.github.askmeagain.meshinery.core.utils.decorators.TestConnectorDecoratorFactory;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;

@MockBean(DataContextInjectApiController.class)
@SpringBootTest(classes = {MeshineryAutoConfiguration.class, DecoratorBeansTest.TestDecoratorConfiguration.class})
class DecoratorBeansTest {

  @Autowired
  ConnectorDecoratorFactory decorator;
  @SpyBean
  MemoryConnector<String, TestContext> connector;

  @Test
  void autoConfigTest(@Autowired RoundRobinScheduler scheduler) throws InterruptedException {
    //Arrange ----------------------------------------------------------------------------------------------------------
    connector.writeOutput("Abc", TestContext.builder()
            .id("a")
        .build());

    //Act --------------------------------------------------------------------------------------------------------------
    //Assert -----------------------------------------------------------------------------------------------------------
    assertThat(scheduler).isNotNull();

    Thread.sleep(3000);

    Mockito.verify(decorator, Mockito.atLeastOnce()).wrap(any());
    Mockito.verify(connector, Mockito.atLeastOnce()).getInputs(any());
  }


  @Configuration
  public static class TestDecoratorConfiguration {

    @Bean
    public MemoryConnector<String, TestContext> memoryConnector(){
      return new MemoryConnector<>();
    }

    @Bean
    public ConnectorDecoratorFactory connectorDecoratorFactory() {
      return Mockito.spy(new TestConnectorDecoratorFactory());
    }

    @Bean
    public MeshineryTask<String, TestContext> task(MemoryConnector<String, TestContext> connector) {
      return MeshineryTaskFactory.<String, TestContext>builder()
          .read(Executors.newSingleThreadExecutor(), "Abc")
          .connector(connector)
          .build();
    }
  }
}
