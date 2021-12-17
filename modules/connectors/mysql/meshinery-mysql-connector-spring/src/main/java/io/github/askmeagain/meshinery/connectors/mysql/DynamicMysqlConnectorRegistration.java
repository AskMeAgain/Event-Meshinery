package io.github.askmeagain.meshinery.connectors.mysql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.core.common.DataContext;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

@RequiredArgsConstructor
public class DynamicMysqlConnectorRegistration implements BeanDefinitionRegistryPostProcessor {

  private final ApplicationContext applicationContext;
  private final ObjectProvider<ObjectMapper> objectMapper;
  private final ObjectProvider<MeshineryMysqlProperties> meshineryMysqlProperties;
  private final ObjectProvider<Jdbi> jdbi;

  private static ResolvableType getTargetType(Class<? extends DataContext> clazz) {
    return ResolvableType.forClassWithGenerics(MysqlConnector.class, clazz);
  }

  private static String getBeanName(Class<? extends DataContext> clazz) {
    return clazz.getSimpleName() + "-auto-generated-mysql-connector-bean";
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    var beanNamesForAnnotation = applicationContext.getBeansWithAnnotation(EnableMeshineryMysql.class);

    for (var tuple : beanNamesForAnnotation.entrySet()) {
      var result = applicationContext.findAnnotationOnBean(tuple.getKey(), EnableMeshineryMysql.class);
      Arrays.stream(result.context())
          .forEach(clazz -> {
            var beanDefinition = new RootBeanDefinition(
                MysqlConnector.class,
                () -> new MysqlConnector<>(
                    getBeanName(clazz),
                    clazz,
                    jdbi.getObject(),
                    objectMapper.getObject(),
                    meshineryMysqlProperties.getObject()
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
