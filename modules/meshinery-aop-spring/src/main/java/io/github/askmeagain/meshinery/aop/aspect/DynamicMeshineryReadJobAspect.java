package io.github.askmeagain.meshinery.aop.aspect;

import io.github.askmeagain.meshinery.aop.MeshineryAopUtils;
import io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.OutputSource;
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

  private final OutputSource<String, DataContext> outputSource;

  @Around("@annotation(io.github.askmeagain.meshinery.aop.common.MeshineryTaskBridge)")
  public void writeToConnectorAspect(ProceedingJoinPoint proceedingJoinPoint) {
    var signature = (MethodSignature) proceedingJoinPoint.getSignature();
    var method = signature.getMethod();
    var annotation = method.getAnnotation(MeshineryTaskBridge.class);
    var arg = proceedingJoinPoint.getArgs()[0];
    var event = MeshineryAopUtils.calculateEventName(annotation, method, proceedingJoinPoint.getTarget());
    outputSource.writeOutput(event, (DataContext) arg, TaskData.ofPropertyList(annotation.properties()));
  }
}