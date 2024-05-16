package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class DynamicJobRegistrar implements BeanDefinitionRegistryPostProcessor, BeanPostProcessor {

  private final ApplicationContext applicationContext;
  private final ExecutorService executorService;

  private ConcurrentHashMap<String, Method> lookupMap = new ConcurrentHashMap<>();

  private static ResolvableType getTargetType(Class<?> contextClazz) {
    return ResolvableType.forClassWithGenerics(MeshineryTask.class, String.class, contextClazz);
  }

  private static String getBeanName(Class<?> clazz) {
    if (MeshineryTask.class.isAssignableFrom(clazz)) {
      int i = 0;
    }
    return clazz.getSimpleName() + "abnc";
  }


  public Object postProcessBeforeInitialization(Object obj, String beanName) throws BeansException {

    Class<?> objClz = obj.getClass();
    if (AopUtils.isAopProxy(obj)) {
      objClz = AopUtils.getTargetClass(obj);
    }

    for (var m : objClz.getDeclaredMethods()) {
      if (m.isAnnotationPresent(MeshineryReadTask.class)) {
        lookupMap.put(beanName, m);
      }
    }

    return obj;
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    var resolvableType = ResolvableType.forClassWithGenerics(MeshineryConnector.class, String.class, DataContext.class);
    ObjectProvider<MeshineryConnector<String, DataContext>> provider =
        applicationContext.getBeanProvider(resolvableType);

    for (var beanName : applicationContext.getBeanDefinitionNames()) {
      var clazz = applicationContext.getType(beanName);
      if (AopUtils.isAopProxy(clazz)) {
        clazz = AopUtils.getTargetClass(clazz);
      }

      for (var m : clazz.getDeclaredMethods()) {
        if (m.isAnnotationPresent(MeshineryReadTask.class)) {
          var beanDefinition = new RootBeanDefinition(
              MeshineryTask.class, () -> getBuild(m, applicationContext.getBean(beanName), executorService, provider)
          );
          beanDefinition.setTargetType(getTargetType(clazz));
          registry.registerBeanDefinition(getBeanName(clazz), beanDefinition);
        }
      }
    }
  }

  private static MeshineryTask<String, DataContext> getBuild(
      Method m,
      Object instance,
      ExecutorService executorService,
      ObjectProvider<MeshineryConnector<String, DataContext>> provider
  ) {
    var annotation = m.getAnnotation(MeshineryReadTask.class);

    var dataContextClass = annotation.context();
    var read = annotation.event();
    var unproxiedObject = AopProxyUtils.getSingletonTarget(instance);

    return MeshineryTaskFactory.<String, DataContext>builder()
        .connector(provider.getObject())
        .read(executorService, read)
        .process(new MeshineryProcessor<>() {
          @SneakyThrows
          @Override
          public CompletableFuture<DataContext> processAsync(DataContext context, Executor executor) {
            log.info("proxied method execution?");
            m.invoke(unproxiedObject, context);
            return CompletableFuture.completedFuture(null);
          }
        })
        .build();
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory)
      throws BeansException {
    //empty
  }
}
