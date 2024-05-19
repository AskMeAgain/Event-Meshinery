package io.github.askmeagain.meshinery.aop;

import io.github.askmeagain.meshinery.aop.common.MeshineryReadTask;
import java.lang.reflect.Method;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeshineryAopUtils {

  public static String calculateEventName(MeshineryReadTask annotation, Method methodHandle, Object unproxiedObject) {
    if (!annotation.event().equals("-")) {
      return annotation.event();
    }
    return unproxiedObject.getClass().getSimpleName() + "." + methodHandle.getName();
  }
}
