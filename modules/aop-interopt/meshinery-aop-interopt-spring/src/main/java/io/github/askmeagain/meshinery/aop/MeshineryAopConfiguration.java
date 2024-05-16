package io.github.askmeagain.meshinery.aop;

import java.util.concurrent.ExecutorService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAspectJAutoProxy
@Import(DynamicMeshineryReadJobAspect.class)
@ConditionalOnProperty(prefix = "meshinery.aop", value = "enabled", havingValue = "true")
public class MeshineryAopConfiguration {

  //@Bean
  public static DynamicJobRegistrar dynamicJobRegistrar(
      ApplicationContext applicationContext,
      ExecutorService executorService
  ) {
    return new DynamicJobRegistrar(applicationContext, executorService);
  }
}