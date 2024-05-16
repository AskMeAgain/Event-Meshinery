package io.github.askmeagain.meshinery.aop.aspect;

import io.github.askmeagain.meshinery.aop.common.MeshineryReadTask;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
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

  private final MeshineryConnector<String, DataContext> connector;

  @Around("@annotation(io.github.askmeagain.meshinery.aop.common.MeshineryReadTask)")
  public void writeToConnectorAspect(ProceedingJoinPoint proceedingJoinPoint) {
    //we just do nothing
    var signature = (MethodSignature) proceedingJoinPoint.getSignature();
    var method = signature.getMethod();
    var myAnnotation = method.getAnnotation(MeshineryReadTask.class);

    connector.writeOutput(myAnnotation.event(), (DataContext) proceedingJoinPoint.getArgs()[0]);
  }
}