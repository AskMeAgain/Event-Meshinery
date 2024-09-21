package io.github.askmeagain.meshinery.aop.aspect;

import io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge;
import io.github.askmeagain.meshinery.aop.utils.MeshineryAopUtils;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.task.TaskData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Slf4j
@Aspect
@RequiredArgsConstructor
public class DynamicMeshineryReadJobAspect {

  private final MeshineryOutputSource<String, MeshineryDataContext> outputSource;

  @Around("@annotation(io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge)")
  public void writeToConnectorAspect(ProceedingJoinPoint proceedingJoinPoint) {
    var signature = (MethodSignature) proceedingJoinPoint.getSignature();
    var method = signature.getMethod();
    var annotation = method.getAnnotation(MeshineryTaskBridge.class);
    var arg = proceedingJoinPoint.getArgs()[0];
    var event = MeshineryAopUtils.calculateNewEventName(annotation, method);

    log.error("Writing to: {}", event);
    outputSource.writeOutput(event, (MeshineryDataContext) arg, TaskData.ofPropertyList(annotation.properties()));
  }
}