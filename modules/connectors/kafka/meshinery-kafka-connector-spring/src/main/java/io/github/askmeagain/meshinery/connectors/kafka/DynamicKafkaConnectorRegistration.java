package io.github.askmeagain.meshinery.connectors.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.kafka.sources.KafkaConnector;
import io.github.askmeagain.meshinery.core.common.DataContext;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class DynamicKafkaConnectorRegistration implements BeanDefinitionRegistryPostProcessor {

  private final ApplicationContext applicationContext;
  private final ObjectMapper objectMapper;
  private final ObjectProvider<MeshineryKafkaProperties> meshineryKafkaProperties;

  private static ResolvableType getTargetType(Class<? extends DataContext> clazz) {
    return ResolvableType.forClassWithGenerics(KafkaConnector.class, clazz);
  }

  private static String getBeanName(Class<? extends DataContext> clazz) {
    return clazz.getSimpleName() + "-auto-generated-kafka-connector-bean";
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    var beanNamesForAnnotation = applicationContext.getBeansWithAnnotation(EnableMeshineryKafka.class);

    for (var tuple : beanNamesForAnnotation.entrySet()) {
      var result = applicationContext.findAnnotationOnBean(tuple.getKey(), EnableMeshineryKafka.class);
      Arrays.stream(result.context())
          .forEach(clazz -> {
            var beanDefinition = new RootBeanDefinition(
                KafkaConnector.class,
                () -> new KafkaConnector(clazz, objectMapper, meshineryKafkaProperties.getObject())
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
