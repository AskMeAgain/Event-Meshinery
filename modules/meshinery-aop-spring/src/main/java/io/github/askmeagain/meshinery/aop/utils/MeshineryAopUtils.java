package io.github.askmeagain.meshinery.aop.utils;

import io.github.askmeagain.meshinery.aop.common.MeshineryAopTask;
import io.github.askmeagain.meshinery.aop.config.AopFutureHolderService;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;

@UtilityClass
@Slf4j
public class MeshineryAopUtils {

  public static String calculateNewEventName(MeshineryAopTask annotation, Method methodHandle) {
    if (!annotation.event().isEmpty()) {
      return annotation.event();
    }
    return methodHandle.getName();
  }

  public static Object tryUnproxyingObject(Object beanInstance) {
    var unproxiedObject = AopProxyUtils.getSingletonTarget(beanInstance);
    if (unproxiedObject == null) {
      unproxiedObject = beanInstance;
    }
    return unproxiedObject;
  }

  public static MeshineryDataContext executeMethodHandle(
      MeshineryDataContext context,
      Method methodHandle,
      Object unproxiedObject,
      Class<?> responseType,
      String inputKey,
      AopFutureHolderService aopFutureHolderService
  ) throws IllegalAccessException, InvocationTargetException {

    var responseObj = methodHandle.invoke(unproxiedObject, context);
    var key = inputKey + "_" + context.getId();

    MeshineryDataContext responseCtx = null;
    if (MeshineryDataContext.class.isAssignableFrom(responseType)) {
      responseCtx = (MeshineryDataContext) responseObj;
    }

    log.trace("Looking for inputKey {}", inputKey);
    var future = aopFutureHolderService.getFuture(key);
    if (future != null) {
      future.complete(responseCtx);
    }

    return responseCtx;
  }
}