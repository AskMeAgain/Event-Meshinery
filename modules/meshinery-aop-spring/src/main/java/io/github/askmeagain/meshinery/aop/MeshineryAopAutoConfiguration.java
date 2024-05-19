package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.aop.aspect.DynamicMeshineryReadJobAspect;
import io.github.askmeagain.meshinery.aop.config.DynamicJobAopRegistrar;
import io.github.askmeagain.meshinery.aop.properties.MeshineryAopProperties;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.util.concurrent.ExecutorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@ComponentScan
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(MeshineryAopProperties.class)
@ConditionalOnProperty(prefix = "meshinery.aop", value = "enabled", havingValue = "true", matchIfMissing = true)
public class MeshineryAopAutoConfiguration {

  @Bean
  public DynamicMeshineryReadJobAspect dynamicMeshineryReadJobAspect(
      MeshineryConnector<String, ? extends DataContext> connector
  ) {
    return new DynamicMeshineryReadJobAspect((MeshineryConnector<String, DataContext>) connector);
  }

  @Bean
  public static DynamicJobAopRegistrar dynamicJobRegistrar(
      ApplicationContext applicationContext,
      ExecutorService executorService
  ) {
    return new DynamicJobAopRegistrar(applicationContext, executorService);
  }
}