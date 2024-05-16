package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class DynamicJobRegistrar implements BeanDefinitionRegistryPostProcessor {

  private final ApplicationContext applicationContext;
  private final ExecutorService executorService;

  private static ResolvableType getTargetType(Class<? extends DataContext> contextClazz) {
    return ResolvableType.forClassWithGenerics(MeshineryTaskFactory.class, String.class, contextClazz);
  }

  private static String getBeanName(Class<? extends DataContext> clazz) {
    return clazz.getSimpleName() + "-auto-generated-kafka-connector-bean";
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    for (String beanName : applicationContext.getBeanDefinitionNames()) {
      try {
        extracted(registry, beanName);
      } catch (Exception e) {

      }
    }
  }

  private void extracted(BeanDefinitionRegistry registry, String beanName) {
    Object obj = applicationContext.getBean(beanName); //at this point beans dont exist yet sometimes

    var provider = applicationContext.getBeanProvider(MeshineryConnector.class);

    Class<?> objClz = obj.getClass();
    if (org.springframework.aop.support.AopUtils.isAopProxy(obj)) {
      objClz = org.springframework.aop.support.AopUtils.getTargetClass(obj);
    }

    for (var m : objClz.getDeclaredMethods()) {
      if (m.isAnnotationPresent(MeshineryReadTask.class)) {
        var annotation = m.getAnnotation(MeshineryReadTask.class);

        var dataContextClass = annotation.context();
        var read = annotation.event();

        var beanDefinition = new RootBeanDefinition(
            io.github.askmeagain.meshinery.core.task.MeshineryTask.class,
            () -> getBuild(m, executorService, read, provider)
        );
        beanDefinition.setTargetType(getTargetType(dataContextClass));
        registry.registerBeanDefinition(getBeanName(dataContextClass), beanDefinition);
      }
    }
  }

  private static io.github.askmeagain.meshinery.core.task.MeshineryTask<String, DataContext> getBuild(
      Method m,
      ExecutorService executorService,
      String read,
      ObjectProvider<MeshineryConnector> provider
  ) {
    return MeshineryTaskFactory.<String, DataContext>builder()
        .connector(provider.getObject())
        .read(executorService, read)
        .process(new MeshineryProcessor<>() {
          @SneakyThrows
          @Override
          public CompletableFuture<DataContext> processAsync(DataContext context, Executor executor) {
            log.info("proxied method execution?");
            return CompletableFuture.completedFuture((DataContext) m.invoke(context));
          }
        })
        .build();
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    //empty
  }
}
