package io.github.askmeagain.meshinery.aop.processors;

import io.github.askmeagain.meshinery.aop.config.AopFutureHolderService;
import io.github.askmeagain.meshinery.aop.utils.MeshineryAopUtils;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AopJobNoRetryProcessor implements MeshineryProcessor<MeshineryDataContext, MeshineryDataContext> {
  private final Method methodHandle;
  private final Object unproxiedObject;
  private final Class<?> responseType;
  private final String inputKey;
  private final AopFutureHolderService aopFutureHolderService;

  @SneakyThrows
  @Override
  public MeshineryDataContext process(MeshineryDataContext context) {
    try {
      return MeshineryAopUtils.executeMethodHandle(
          context,
          methodHandle,
          unproxiedObject,
          responseType,
          inputKey,
          aopFutureHolderService
      );
    } catch (Exception e) {
      var future = aopFutureHolderService.getFuture(inputKey + "_" + context.getId());
      if (future != null) {
        future.completeExceptionally(e);
      }
      throw e;
    }
  }
}