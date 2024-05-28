package io.github.askmeagain.meshinery.aop.config;

import io.github.askmeagain.meshinery.aop.MeshineryAopUtils;
import io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge;
import io.github.askmeagain.meshinery.aop.exception.MeshineryAopWrongMethodParameterType;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import java.lang.reflect.Method;
import java.util.List;
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
public class DynamicJobAopRegistrar implements BeanDefinitionRegistryPostProcessor {

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
    for (var proxiedBeanName : applicationContext.getBeanDefinitionNames()) {
      var clazz = applicationContext.getType(proxiedBeanName);
      if (AopUtils.isAopProxy(clazz)) {
        clazz = AopUtils.getTargetClass(clazz);
      }

      for (var m : clazz.getDeclaredMethods()) {
        var targetType = getTargetType(clazz);
        var newBeanName = getBeanName(clazz);

        if (m.isAnnotationPresent(MeshineryTaskBridge.class)) {
          var beanDefinition = new RootBeanDefinition(
              MeshineryTask.class,
              () -> buildMeshineryJob(
                  m,
                  applicationContext.getBean(proxiedBeanName),
                  executorService,
                  applicationContext.getBeanProvider(ResolvableType.forClassWithGenerics(
                      MeshinerySourceConnector.class,
                      String.class,
                      m.getParameterTypes()[0]
                  ))
              )
          );
          beanDefinition.setTargetType(targetType);
          registry.registerBeanDefinition(newBeanName, beanDefinition);
        }
      }
    }
  }

  private static MeshineryTask<String, MeshineryDataContext> buildMeshineryJob(
      Method methodHandle,
      Object beanInstance,
      ExecutorService executorService,
      ObjectProvider<MeshinerySourceConnector<String, MeshineryDataContext>> provider
  ) {
    var unproxiedObject = AopProxyUtils.getSingletonTarget(beanInstance);

    var annotation = methodHandle.getAnnotation(MeshineryTaskBridge.class);
    var properties = annotation.properties();
    var readEvent = MeshineryAopUtils.calculateEventName(annotation, methodHandle, unproxiedObject);
    var writeEvent = annotation.write().equals("-") ? new String[0] : new String[]{annotation.write()};

    var contextClazz = methodHandle.getParameterTypes()[0];
    var responseType = methodHandle.getReturnType();

    if (!MeshineryDataContext.class.isAssignableFrom(contextClazz)) {
      throw new MeshineryAopWrongMethodParameterType(methodHandle);
    }

    return MeshineryTaskFactory.<String, MeshineryDataContext>builder()
        .connector(provider.getObject())
        .taskName(annotation.taskName().equals("-") ? "dynamic-job-" + readEvent.toLowerCase() : annotation.taskName())
        .putData(List.of(properties))
        .read(executorService, readEvent)
        .process(new MeshineryProcessor<>() {
          @SneakyThrows
          @Override
          public MeshineryDataContext processAsync(MeshineryDataContext context) {
            var response = methodHandle.invoke(unproxiedObject, context);
            if (MeshineryDataContext.class.isAssignableFrom(responseType)) {
              return (MeshineryDataContext) response;
            } else {
              return null;
            }
          }
        })
        .write(writeEvent)
        .build();
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
    //empty
  }
}
