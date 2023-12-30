package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import io.github.askmeagain.meshinery.connectors.pubsub.nameresolver.PubSubNameResolver;
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
public class DynamicPubSubConnectorRegistration implements BeanDefinitionRegistryPostProcessor {

  private final ApplicationContext applicationContext;
  private final ObjectProvider<ObjectMapper> objectMapper;
  private final ObjectProvider<MeshineryPubSubProperties> meshineryPostgresProperties;
  private final ObjectProvider<MeshineryTransportChannelProvider> transportChannelProviders;
  private final ObjectProvider<CredentialsProvider> credentialsProviders;
  private final ObjectProvider<PubSubNameResolver> pubSubNameResolvers;

  private static ResolvableType getTargetType(Class<? extends DataContext> clazz) {
    return ResolvableType.forClassWithGenerics(PubSubConnector.class, clazz);
  }

  private static String getBeanName(Class<? extends DataContext> clazz) {
    return clazz.getSimpleName() + "-auto-generated-pubsub-connector-bean";
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    var beanNamesForAnnotation = applicationContext.getBeansWithAnnotation(EnableMeshineryPubSub.class);

    for (var tuple : beanNamesForAnnotation.entrySet()) {
      var result = applicationContext.findAnnotationOnBean(tuple.getKey(), EnableMeshineryPubSub.class);
      Arrays.stream(result.context())
          .forEach(clazz -> {
            var beanDefinition = new RootBeanDefinition(
                PubSubConnector.class,
                () -> new PubSubConnector<>(
                    getBeanName(clazz),
                    clazz,
                    objectMapper.getObject(),
                    meshineryPostgresProperties.getObject(),
                    transportChannelProviders.getObject().getTransportChannelProvider(),
                    credentialsProviders.getObject(),
                    pubSubNameResolvers.getObject()
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
