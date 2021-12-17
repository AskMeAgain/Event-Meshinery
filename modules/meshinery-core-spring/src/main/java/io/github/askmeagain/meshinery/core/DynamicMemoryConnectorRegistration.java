package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.source.MemoryConnector;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

@RequiredArgsConstructor
public class DynamicMemoryConnectorRegistration implements BeanDefinitionRegistryPostProcessor {

  private final ApplicationContext applicationContext;

  private static ResolvableType getTargetType(Class<? extends DataContext> clazz) {
    return ResolvableType.forClassWithGenerics(MemoryConnector.class, String.class, clazz);
  }

  private static String getBeanName(Class<? extends DataContext> clazz) {
    return clazz.getSimpleName() + "-auto-generated-memory-connector-bean";
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    var beanNamesForAnnotation = applicationContext.getBeansWithAnnotation(EnableMeshinery.class);

    for (var tuple : beanNamesForAnnotation.entrySet()) {
      var result = applicationContext.findAnnotationOnBean(tuple.getKey(), EnableMeshinery.class);
      Arrays.stream(result.context())
          .forEach(clazz -> {
            var beanDefinition = new RootBeanDefinition(
                MemoryConnector.class,
                () -> new MemoryConnector<>(getBeanName(clazz))
            );
            beanDefinition.setTargetType(getTargetType(clazz));
            registry.registerBeanDefinition(getBeanName(clazz), beanDefinition);
          });
    }
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    //empty
  }
}
