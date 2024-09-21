package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.aop.registrar.DynamicInMemoryJobAopRegistrar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class RegistrarConfiguration {

  @Bean
  public static DynamicInMemoryJobAopRegistrar abc(ApplicationContext applicationContext) {
    return new DynamicInMemoryJobAopRegistrar(applicationContext);
  }
}