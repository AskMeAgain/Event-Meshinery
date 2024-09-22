package io.github.askmeagain.meshinery.aop.processor;

import io.github.askmeagain.meshinery.aop.utils.MeshineryAopUtils;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryProcessor;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AopJobProcessor implements MeshineryProcessor<MeshineryDataContext, MeshineryDataContext> {
  private final Method methodHandle;
  private final Object unproxiedObject;
  private final Class<?> responseType;
  private final String inputKey;

  @SneakyThrows
  @Override
  public MeshineryDataContext process(MeshineryDataContext context) {
    return MeshineryAopUtils.executeMethodHandle(
        context,
        methodHandle,
        unproxiedObject,
        responseType,
        inputKey
    );
  }
}