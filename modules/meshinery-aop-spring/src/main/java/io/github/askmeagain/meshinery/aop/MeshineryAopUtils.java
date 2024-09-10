package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge;
import java.lang.reflect.Method;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeshineryAopUtils {

  public static String calculateEventName(MeshineryTaskBridge annotation, Method methodHandle, Object unproxiedObject) {
    if (!annotation.event().isEmpty()) {
      return annotation.event();
    }
    return methodHandle.getName();
  }
}
