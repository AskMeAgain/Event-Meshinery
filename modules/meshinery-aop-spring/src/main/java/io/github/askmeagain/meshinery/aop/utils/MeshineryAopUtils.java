package io.github.askmeagain.meshinery.aop.utils;

import io.github.askmeagain.meshinery.aop.aspect.DynamicMeshineryReadJobAspect;
import io.github.askmeagain.meshinery.aop.common.MeshineryAopTask;
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
      String inputKey
  ) throws IllegalAccessException, InvocationTargetException {

    var responseObj = methodHandle.invoke(unproxiedObject, context);
    var key = inputKey + "_" + context.getId();

    MeshineryDataContext responseCtx = null;
    if (MeshineryDataContext.class.isAssignableFrom(responseType)) {
      responseCtx = (MeshineryDataContext) responseObj;
    }

    log.error("Looking for key {}", inputKey);
    var future = DynamicMeshineryReadJobAspect.FUTURES.get(key);
    if (future != null) {
      future.complete(responseCtx);
    }

    return responseCtx;
  }
}