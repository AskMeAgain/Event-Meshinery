package io.github.askmeagain.meshinery.connectors.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.postgres.MeshineryPostgresProperties;
import io.github.askmeagain.meshinery.connectors.postgres.PostgresConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
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
public class DynamicPostgresConnectorRegistration implements BeanDefinitionRegistryPostProcessor {

  private final ApplicationContext applicationContext;
  private final ObjectProvider<ObjectMapper> objectMapper;
  private final ObjectProvider<MeshineryPostgresProperties> meshineryPostgresProperties;

  private static ResolvableType getTargetType(Class<? extends MeshineryDataContext> clazz) {
    return ResolvableType.forClassWithGenerics(PostgresConnector.class, clazz);
  }

  private static String getBeanName(Class<? extends MeshineryDataContext> clazz) {
    return clazz.getSimpleName() + "-auto-generated-postgres-connector-bean";
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    var beanNamesForAnnotation = applicationContext.getBeansWithAnnotation(EnableMeshineryPostgres.class);

    for (var tuple : beanNamesForAnnotation.entrySet()) {
      var result = applicationContext.findAnnotationOnBean(tuple.getKey(), EnableMeshineryPostgres.class);
      Arrays.stream(result.context())
          .forEach(clazz -> {
            var beanDefinition = new RootBeanDefinition(
                PostgresConnector.class,
                () -> new PostgresConnector<>(
                    getBeanName(clazz),
                    clazz,
                    objectMapper.getObject(),
                    meshineryPostgresProperties.getObject()
                )
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
