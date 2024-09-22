package io.github.askmeagain.meshinery.aop.utils;

import io.github.askmeagain.meshinery.aop.common.MeshineryAopTask;
import io.github.askmeagain.meshinery.aop.exception.MeshineryAopWrongMethodParameterType;
import io.github.askmeagain.meshinery.aop.processor.AopJobInMemoryRetryProcessor;
import io.github.askmeagain.meshinery.aop.processor.AopJobProcessor;
import io.github.askmeagain.meshinery.aop.processor.AopJopEventRetryProcessor;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.lang.reflect.Method;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

@Slf4j
public class MeshineryAopJobCreationUtils {

  public static MeshineryTask<String, MeshineryDataContext> buildInMemoryRetryJob(
      Method methodHandle,
      Object beanInstance,
      MeshineryAopTask annotation,
      ObjectProvider<MeshinerySourceConnector<String, MeshineryDataContext>> provider
  ) {
    var unproxiedObject = MeshineryAopUtils.tryUnproxyingObject(beanInstance);
    var properties = annotation.properties();
    var readEvent = MeshineryAopUtils.calculateNewEventName(annotation, methodHandle);
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
        .process(new AopJobInMemoryRetryProcessor(
            annotation.retryOnException(),
            annotation.retryCount(),
            methodHandle,
            unproxiedObject,
            responseType,
            readEvent
        ))
        .write(writeEvent)
        .build();
  }

  public static MeshineryTask<String, MeshineryDataContext> buildSimpleJob(
      Method methodHandle,
      Object beanInstance,
      MeshineryAopTask annotation,
      ObjectProvider<MeshinerySourceConnector<String, MeshineryDataContext>> provider
  ) {
    var unproxiedObject = MeshineryAopUtils.tryUnproxyingObject(beanInstance);
    var properties = annotation.properties();
    var readEvent = MeshineryAopUtils.calculateNewEventName(annotation, methodHandle);
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
        .process(new AopJobProcessor(methodHandle, unproxiedObject, responseType, readEvent))
        .write(writeEvent)
        .build();
  }

  public static MeshineryTask<String, MeshineryDataContext> buildInEventRetryJob(
      int iteration,
      String readEvent,
      String onErrorEvent,
      String onSuccessEvent,
      Method methodHandle,
      String proxiedBeanName,
      ApplicationContext applicationContext,
      MeshineryAopTask annotation
  ) {
    var beanInstance = applicationContext.getBean(proxiedBeanName);
    var unproxiedObject = MeshineryAopUtils.tryUnproxyingObject(beanInstance);
    var properties = annotation.properties();
    var contextClazz = methodHandle.getParameterTypes()[0];
    var responseType = methodHandle.getReturnType();
    var provider = applicationContext.<MeshinerySourceConnector<String, MeshineryDataContext>>getBeanProvider(
        ResolvableType.forClassWithGenerics(
            MeshinerySourceConnector.class,
            String.class,
            methodHandle.getParameterTypes()[0]
        ));

    if (!MeshineryDataContext.class.isAssignableFrom(contextClazz)) {
      throw new MeshineryAopWrongMethodParameterType(methodHandle);
    }

    var connector = provider.getObject();

    var name = "aop-" + readEvent + "-" + iteration + "-of-" + annotation.retryCount();
    if (onErrorEvent == null) {
      name = "aop-" + onSuccessEvent;
    }
    return MeshineryTaskFactory.<String, MeshineryDataContext>builder()
        .connector(connector)
        .taskName(name)
        .putData(List.of(properties))
        .read(readEvent)
        .process(new AopJopEventRetryProcessor(methodHandle, unproxiedObject, responseType, readEvent))
        .exceptionHandler((ctx, exc) -> {
          if (annotation.retryCount() == iteration - 1) {
            //throw new RuntimeException(exc);
            log.error("returning null");
            return null;
          }
          if (onErrorEvent != null) {
            log.info("Retrying {}/{}", iteration, annotation.retryCount());
            connector.writeOutput(onErrorEvent, ctx, new TaskData());
            return null;
          }
          return ctx;
        })
        .write(onSuccessEvent)
        .build();
  }

  private static String calculateTaskName(MeshineryAopTask annotation, String readEvent) {
    return annotation.taskName().isEmpty() ? "aop-" + readEvent.toLowerCase() : annotation.taskName();
  }
}
