package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class DynamicMemoryConnectorRegistration implements BeanDefinitionRegistryPostProcessor {

  private final ApplicationContext applicationContext;

  private static ResolvableType getTargetType(EnableMeshinery.KeyDataContext keyDataContext) {
    return ResolvableType.forClassWithGenerics(MemoryConnector.class, keyDataContext.key(), keyDataContext.context());
  }

  private static String getBeanName(EnableMeshinery.KeyDataContext keyDataContext) {
    return "%s-%s-auto-generated-memory-connector-bean".formatted(keyDataContext.context(), keyDataContext.key());
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    var beanNamesForAnnotation = applicationContext.getBeansWithAnnotation(EnableMeshinery.class);

    for (var tuple : beanNamesForAnnotation.entrySet()) {
      var result = applicationContext.findAnnotationOnBean(tuple.getKey(), EnableMeshinery.class);
      if (result == null) {
        continue;
      }
      for (var container : result.connector()) {
        var beanDefinition = new RootBeanDefinition(
            MemoryConnector.class,
            () -> new MemoryConnector<>(getBeanName(container))
        );
        beanDefinition.setTargetType(getTargetType(container));
        registry.registerBeanDefinition(getBeanName(container), beanDefinition);
      }
    }
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    //empty
  }
}
