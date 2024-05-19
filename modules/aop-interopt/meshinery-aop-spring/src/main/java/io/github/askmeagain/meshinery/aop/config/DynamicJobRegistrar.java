package io.github.askmeagain.meshinery.aop.config;

import io.github.askmeagain.meshinery.aop.common.MeshineryReadTask;
import io.github.askmeagain.meshinery.aop.exception.MeshineryAopWrongMethodParameterType;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
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

  private static ResolvableType getTargetType(Class<?> contextClazz) {
    return ResolvableType.forClassWithGenerics(MeshineryTask.class, String.class, contextClazz);
  }

  private static String getBeanName(Class<?> clazz) {
    return clazz.getSimpleName() + "-dynamic-meshinery-aop-job";
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

    for (var beanName : applicationContext.getBeanDefinitionNames()) {
      var clazz = applicationContext.getType(beanName);
      if (AopUtils.isAopProxy(clazz)) {
        clazz = AopUtils.getTargetClass(clazz);
      }

      for (var m : clazz.getDeclaredMethods()) {
        if (m.isAnnotationPresent(MeshineryReadTask.class)) {
          var beanDefinition = new RootBeanDefinition(
              MeshineryTask.class,
              () -> buildMeshineryJob(
                  m,
                  applicationContext.getBean(beanName),
                  executorService,
                  applicationContext.getBeanProvider(ResolvableType.forClassWithGenerics(
                      MeshineryConnector.class,
                      String.class,
                      m.getParameterTypes()[0]
                  ))
              )
          );
          beanDefinition.setTargetType(getTargetType(clazz));
          registry.registerBeanDefinition(getBeanName(clazz), beanDefinition);
        }
      }
    }
  }

  private static MeshineryTask<String, DataContext> buildMeshineryJob(
      Method methodHandle,
      Object beanInstance,
      ExecutorService executorService,
      ObjectProvider<MeshineryConnector<String, ? extends DataContext>> provider
  ) {

    var read = methodHandle.getName();
    var unproxiedObject = AopProxyUtils.getSingletonTarget(beanInstance);
    var contextClazz = methodHandle.getParameterTypes()[0];

    if (!DataContext.class.isAssignableFrom(contextClazz)) {
      throw new MeshineryAopWrongMethodParameterType(methodHandle);
    }

    return MeshineryTaskFactory.<String, DataContext>builder()
        .connector((MeshineryConnector<String, DataContext>) provider.getObject())
        .read(executorService, read)
        .process(new MeshineryProcessor<>() {
          @SneakyThrows
          @Override
          public CompletableFuture<DataContext> processAsync(DataContext context, Executor executor) {
            methodHandle.invoke(unproxiedObject, context);
            return CompletableFuture.completedFuture(null);
          }
        })
        .build();
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
    //empty
  }
}
