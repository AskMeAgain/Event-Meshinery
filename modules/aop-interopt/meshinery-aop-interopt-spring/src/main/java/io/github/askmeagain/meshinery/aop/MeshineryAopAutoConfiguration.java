package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.util.concurrent.ExecutorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "meshinery.aop", value = "enabled", havingValue = "true")
public class MeshineryAopAutoConfiguration {

  @Bean
  public DynamicMeshineryReadJobAspect dynamicMeshineryReadJobAspect(
      MeshineryConnector<String, DataContext> connector
  ) {
    return new DynamicMeshineryReadJobAspect(connector);
  }

  @Bean
  public static DynamicJobRegistrar dynamicJobRegistrar(
      ApplicationContext applicationContext,
      ExecutorService executorService
  ) {
    return new DynamicJobRegistrar(applicationContext, executorService);
  }
}