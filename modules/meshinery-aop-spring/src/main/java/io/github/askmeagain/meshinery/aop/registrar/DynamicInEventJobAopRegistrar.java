package io.github.askmeagain.meshinery.aop.registrar;

import io.github.askmeagain.meshinery.aop.MeshineryAopUtils;
import io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge;
import io.github.askmeagain.meshinery.aop.config.AopJobReceiverEventRetryProcessor;
import io.github.askmeagain.meshinery.aop.exception.MeshineryAopWrongMethodParameterType;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import java.lang.reflect.Method;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class DynamicInEventJobAopRegistrar implements BeanDefinitionRegistryPostProcessor {

  private final ApplicationContext applicationContext;

  private static ResolvableType getTargetType(Class<?> contextClazz) {
    return ResolvableType.forClassWithGenerics(MeshineryTask.class, String.class, contextClazz);
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    for (var proxiedBeanName : applicationContext.getBeanDefinitionNames()) {
      var clazz = applicationContext.getType(proxiedBeanName);
      if (AopUtils.isAopProxy(clazz)) {
        clazz = AopUtils.getTargetClass(clazz);
      }

      for (var methodHandle : clazz.getDeclaredMethods()) {
        if (methodHandle.isAnnotationPresent(MeshineryTaskBridge.class)) {
          var annotation = methodHandle.getAnnotation(MeshineryTaskBridge.class);
          if (annotation.inMemoryRetry()) {
            continue;
          }
          var targetType = getTargetType(clazz);
          var newBeanName = getBeanName(annotation, methodHandle);

          var beanDefinition = new RootBeanDefinition(
              MeshineryTask.class,
              () -> buildMeshineryJob(
                  methodHandle,
                  applicationContext.getBean(proxiedBeanName),
                  annotation,
                  applicationContext.getBeanProvider(ResolvableType.forClassWithGenerics(
                      MeshinerySourceConnector.class,
                      String.class,
                      methodHandle.getParameterTypes()[0]
                  ))
              )
          );
          beanDefinition.setTargetType(targetType);
          registry.registerBeanDefinition(newBeanName, beanDefinition);
        }
      }
    }
  }

  private static String getBeanName(MeshineryTaskBridge annotation, Method methodHandle) {
    if (annotation.taskName().isBlank()) {
      return methodHandle.getName();
    }
    return annotation.taskName() + "Bean";
  }

  private static MeshineryTask<String, MeshineryDataContext> buildMeshineryJob(
      Method methodHandle,
      Object beanInstance,
      MeshineryTaskBridge annotation,
      ObjectProvider<MeshinerySourceConnector<String, MeshineryDataContext>> provider
  ) {
    var unproxiedObject = AopProxyUtils.getSingletonTarget(beanInstance);

    var properties = annotation.properties();
    var readEvent = MeshineryAopUtils.calculateEventName(annotation, methodHandle, unproxiedObject);
    var writeEvent = annotation.write().isEmpty() ? new String[0] : new String[]{annotation.write()};

    var contextClazz = methodHandle.getParameterTypes()[0];
    var responseType = methodHandle.getReturnType();

    if (!MeshineryDataContext.class.isAssignableFrom(contextClazz)) {
      throw new MeshineryAopWrongMethodParameterType(methodHandle);
    }

    return MeshineryTaskFactory.<String, MeshineryDataContext>builder()
        .connector(provider.getObject())
        .taskName(calculateTaskName(annotation, readEvent))
        .putData(List.of(properties))
        .read(readEvent)
        .process(new AopJobReceiverEventRetryProcessor(
            methodHandle,
            unproxiedObject,
            responseType
        ))
        .write(writeEvent)
        .build();
  }

  private static String calculateTaskName(MeshineryTaskBridge annotation, String readEvent) {
    return annotation.taskName().isEmpty() ? "dynamic-aop-job-" + readEvent.toLowerCase() : annotation.taskName();
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
    //empty
  }

}
