package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.aop.aspect.DynamicMeshineryReadJobAspect;
import io.github.askmeagain.meshinery.aop.properties.MeshineryAopProperties;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.common.OutputSourceDecoratorFactory;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(MeshineryAopProperties.class)
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "meshinery.aop", value = "enabled", havingValue = "true", matchIfMissing = true)
public class MeshineryAopAutoConfiguration {

  @Bean
  public DynamicMeshineryReadJobAspect dynamicMeshineryReadJobAspect(
      MeshineryOutputSource<String, ? extends MeshineryDataContext> connector,
      List<OutputSourceDecoratorFactory> decorators
  ) {
    var decoratedSource = MeshineryUtils.applyDecorator(connector, decorators);
    return new DynamicMeshineryReadJobAspect((MeshineryOutputSource<String, MeshineryDataContext>) decoratedSource);
  }
}