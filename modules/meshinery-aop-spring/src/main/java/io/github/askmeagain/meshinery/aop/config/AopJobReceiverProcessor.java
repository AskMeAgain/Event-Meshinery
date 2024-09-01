package io.github.askmeagain.meshinery.aop.config;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
class AopJobReceiverProcessor implements MeshineryProcessor<MeshineryDataContext, MeshineryDataContext> {
  private final Method methodHandle;
  private final Object unproxiedObject;
  private final Class<?> responseType;

  @SneakyThrows
  @Override
  public MeshineryDataContext process(MeshineryDataContext context) {
    var response = methodHandle.invoke(unproxiedObject, context);
    if (MeshineryDataContext.class.isAssignableFrom(responseType)) {
      return (MeshineryDataContext) response;
    } else {
      return null;
    }
  }
}
