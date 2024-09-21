package io.github.askmeagain.meshinery.aop.utils;

import io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge;
import io.github.askmeagain.meshinery.aop.exception.MeshineryAopWrongMethodParameterType;
import io.github.askmeagain.meshinery.aop.processor.AopJobReceiverEventRetryProcessor;
import io.github.askmeagain.meshinery.aop.processor.AopJobReceiverInMemoryRetryProcessor;
import io.github.askmeagain.meshinery.aop.processor.AopSimpleJobProcessor;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.core.task.MeshineryTaskFactory;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.lang.reflect.Method;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

@Slf4j
public class AopJobCreationUtils {

  public static MeshineryTask<String, MeshineryDataContext> buildInMemoryRetryJob(
      Method methodHandle,
      Object beanInstance,
      MeshineryTaskBridge annotation,
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
        .process(new AopJobReceiverInMemoryRetryProcessor(
            annotation.retryOnException(),
            annotation.retryCount(),
            methodHandle,
            unproxiedObject,
            responseType
        ))
        .write(writeEvent)
        .build();
  }

  public static MeshineryTask<String, MeshineryDataContext> buildSimpleJob(
      Method methodHandle,
      Object beanInstance,
      MeshineryTaskBridge annotation,
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
        .process(new AopSimpleJobProcessor(methodHandle, unproxiedObject, responseType))
        .write(writeEvent)
        .build();
  }

  public static MeshineryTask<String, MeshineryDataContext> buildInEventRetryJob(
      int iteration,
      String readEvent,
      String onErrorEvent,
      String onSuccessEvent,
      Method methodHandle,
      Object unproxiedObject,
      MeshineryTaskBridge annotation,
      ObjectProvider<MeshinerySourceConnector<String, MeshineryDataContext>> provider
  ) {
    var properties = annotation.properties();
    var contextClazz = methodHandle.getParameterTypes()[0];
    var responseType = methodHandle.getReturnType();

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
        .process(new AopJobReceiverEventRetryProcessor(methodHandle, unproxiedObject, responseType))
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

  private static String calculateTaskName(MeshineryTaskBridge annotation, String readEvent) {
    return annotation.taskName().isEmpty() ? "aop-" + readEvent.toLowerCase() : annotation.taskName();
  }
}
