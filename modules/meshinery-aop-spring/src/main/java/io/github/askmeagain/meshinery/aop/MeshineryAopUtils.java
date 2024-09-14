package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge;
import java.lang.reflect.Method;
import lombok.experimental.UtilityClass;
import org.springframework.aop.framework.AopProxyUtils;

@UtilityClass
public class MeshineryAopUtils {

  public static String calculateEventName(MeshineryTaskBridge annotation, Method methodHandle, Object unproxiedObject) {
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
}
