package io.github.askmeagain.meshinery.aop.processor;

import io.github.askmeagain.meshinery.aop.aspect.DynamicMeshineryReadJobAspect;
import io.github.askmeagain.meshinery.aop.utils.MeshineryAopUtils;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AopJobInMemoryRetryProcessor implements MeshineryProcessor<MeshineryDataContext, MeshineryDataContext> {
  private final Class<? extends Exception> retryOnException;
  private final int retryCount;
  private final Method methodHandle;
  private final Object unproxiedObject;
  private final Class<?> responseType;
  private final String inputKey;

  @SneakyThrows
  @Override
  public MeshineryDataContext process(MeshineryDataContext context) {
    var i = 0;
    while (true) {
      i++;
      try {
        return MeshineryAopUtils.executeMethodHandle(context, methodHandle, unproxiedObject, responseType, inputKey);
      } catch (InvocationTargetException e) {
        if (i > retryCount) {
          log.error("Retry count reached, killing future exceptionally {}", inputKey);
          var future = DynamicMeshineryReadJobAspect.FUTURES.get(inputKey + "_" + context.getId());
          if (future != null) {
            future.completeExceptionally(e);
          }
          throw e.getCause();
        }
        if (retryOnException.isAssignableFrom(e.getTargetException().getClass())) {
          log.error("Retrying {}/{}", i, retryCount, e);
          continue;
        }
        log.error("Not right exception type found: {}", inputKey);
        var future = DynamicMeshineryReadJobAspect.FUTURES.get(inputKey + "_" + context.getId());
        if (future != null) {
          future.completeExceptionally(e);
        }
        throw e.getCause();
      }
    }
  }
}
