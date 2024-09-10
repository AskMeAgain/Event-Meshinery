package io.github.askmeagain.meshinery.aop.processor;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AopJobReceiverInMemoryRetryProcessor
    implements MeshineryProcessor<MeshineryDataContext, MeshineryDataContext> {
  private final Class<Exception> retryOnException;
  private final int retryCount;
  private final Method methodHandle;
  private final Object unproxiedObject;
  private final Class<?> responseType;

  @SneakyThrows
  @Override
  public MeshineryDataContext process(MeshineryDataContext context) {
    var i = 0;
    while (true) {
      i++;
      try {
        return executeMethod(context);
      } catch (InvocationTargetException e) {
        if (i > retryCount) {
          throw e;
        }
        if (retryOnException.isAssignableFrom(e.getTargetException().getClass())) {
          log.error("Retrying {}/{}", i, retryCount, e);
          continue;
        }
        throw e;
      }
    }
  }

  private MeshineryDataContext executeMethod(MeshineryDataContext context)
      throws InvocationTargetException, IllegalAccessException {
    var response = methodHandle.invoke(unproxiedObject, context);
    if (MeshineryDataContext.class.isAssignableFrom(responseType)) {
      return (MeshineryDataContext) response;
    } else {
      return null;
    }
  }
}
