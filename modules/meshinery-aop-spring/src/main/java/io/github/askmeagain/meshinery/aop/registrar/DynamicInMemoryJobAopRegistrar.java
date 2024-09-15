package io.github.askmeagain.meshinery.aop.registrar;

import io.github.askmeagain.meshinery.aop.MeshineryAopUtils;
import io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge;
import io.github.askmeagain.meshinery.aop.common.RetryType;
import io.github.askmeagain.meshinery.aop.utils.AopJobCreationUtils;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.lang.reflect.Method;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class DynamicInMemoryJobAopRegistrar implements BeanDefinitionRegistryPostProcessor {

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
          try {
            doRegisterBeans(registry, proxiedBeanName, methodHandle, clazz, annotation);
          } catch (Exception e) {
            log.error("Failed to create aop job", e);
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

  private void doRegisterBeans(
      BeanDefinitionRegistry registry,
      String proxiedBeanName,
      Method methodHandle,
      Class<?> clazz,
      MeshineryTaskBridge annotation
  ) {
    var targetType = getTargetType(clazz);
    var newBeanName = getBeanName(annotation, methodHandle);

    var beans = new HashMap<String, RootBeanDefinition>();

    if (annotation.inMemoryRetry() == RetryType.MEMORY) {
      var beanDefinition = new RootBeanDefinition(
          MeshineryTask.class,
          () -> AopJobCreationUtils.buildInMemoryRetryJob(
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
      beans.put(newBeanName, beanDefinition);
    } else if (annotation.inMemoryRetry() == RetryType.EVENT) {
      var beanInstance = applicationContext.getBean(proxiedBeanName);
      var unproxiedObject = MeshineryAopUtils.tryUnproxyingObject(beanInstance);
      var readEvent = MeshineryAopUtils.calculateNewEventName(annotation, methodHandle);
      var beginningJob = new RootBeanDefinition(
          MeshineryTask.class,
          () -> AopJobCreationUtils.buildInEventRetryJob(
              1,
              readEvent,
              readEvent + "-0",
              annotation.write(),
              methodHandle,
              unproxiedObject,
              annotation,
              applicationContext.getBeanProvider(ResolvableType.forClassWithGenerics(
                  MeshinerySourceConnector.class,
                  String.class,
                  methodHandle.getParameterTypes()[0]
              ))
          )
      );
      beans.put(newBeanName + "-0", beginningJob);

      for (var i = 1; i < annotation.retryCount(); i++) {
        int finalI = i;
        var intermediateJob = new RootBeanDefinition(
            MeshineryTask.class,
            () -> AopJobCreationUtils.buildInEventRetryJob(
                finalI + 1,
                readEvent + "-" + (finalI - 1),
                readEvent + "-" + (finalI),
                annotation.write(),
                methodHandle,
                unproxiedObject,
                annotation,
                applicationContext.getBeanProvider(ResolvableType.forClassWithGenerics(
                    MeshinerySourceConnector.class,
                    String.class,
                    methodHandle.getParameterTypes()[0]
                ))
            )
        );
        beans.put(newBeanName + "-retry-" + i, intermediateJob);
      }

      var endJob = new RootBeanDefinition(
          MeshineryTask.class,
          () -> AopJobCreationUtils.buildInEventRetryJob(
              annotation.retryCount(),
              readEvent + "-" + (annotation.retryCount() - 1),
              null,
              annotation.write(),
              methodHandle,
              unproxiedObject,
              annotation,
              applicationContext.getBeanProvider(ResolvableType.forClassWithGenerics(
                  MeshinerySourceConnector.class,
                  String.class,
                  methodHandle.getParameterTypes()[0]
              ))
          )
      );
      beans.put(newBeanName + "-end", endJob);
    } else {
      var beanDefinition = new RootBeanDefinition(
          MeshineryTask.class,
          () -> AopJobCreationUtils.buildSimpleJob(
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
      beans.put(newBeanName, beanDefinition);
    }

    //registering the beans now
    beans.forEach((name, beanDefinition) -> {
      beanDefinition.setTargetType(targetType);
      registry.registerBeanDefinition(name, beanDefinition);
    });
  }

  private static String getBeanName(MeshineryTaskBridge annotation, Method methodHandle) {
    if (annotation.taskName().isBlank()) {
      return methodHandle.getName() + "Bean";
    }
    return annotation.taskName() + "Bean";
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
    //empty
  }
}