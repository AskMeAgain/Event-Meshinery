package io.github.askmeagain.meshinery.aop.processor;

import io.github.askmeagain.meshinery.aop.aspect.DynamicMeshineryReadJobAspect;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AopSimpleJobProcessor implements MeshineryProcessor<MeshineryDataContext, MeshineryDataContext> {
  private final Method methodHandle;
  private final Object unproxiedObject;
  private final Class<?> responseType;
  private final String inputKey;

  @SneakyThrows
  @Override
  public MeshineryDataContext process(MeshineryDataContext context) {
    var responseObj = methodHandle.invoke(unproxiedObject, context);
    if (MeshineryDataContext.class.isAssignableFrom(responseType)) {
      var future = DynamicMeshineryReadJobAspect.FUTURES.get(inputKey + "_" + context.getId());
      var responseCtx = (MeshineryDataContext) responseObj;
      if (future != null) {
        future.complete(responseCtx);
      }
      return responseCtx;
    } else {
      return null;
    }
  }

}
