package io.github.askmeagain.meshinery.aop.aspect;

import io.github.askmeagain.meshinery.aop.common.MeshineryAopTask;
import io.github.askmeagain.meshinery.aop.config.AopFutureHolderService;
import io.github.askmeagain.meshinery.aop.utils.MeshineryAopUtils;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryOutputSource;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.concurrent.ExecutionException;
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
  private final AopFutureHolderService aopFutureHolderService;

  @Around("@annotation(io.github.askmeagain.meshinery.aop.common.MeshineryAopTask)")
  public Object writeToConnectorAspect(ProceedingJoinPoint pjp) throws ExecutionException, InterruptedException {
    var signature = (MethodSignature) pjp.getSignature();
    var method = signature.getMethod();
    var annotation = method.getAnnotation(MeshineryAopTask.class);
    var arg = pjp.getArgs()[0];
    var event = MeshineryAopUtils.calculateNewEventName(annotation, method);

    var context = (MeshineryDataContext) arg;

    var future = aopFutureHolderService.createFuture(event + "_" + context.getId());
    log.trace("Awaiting result of event {}", event);

    outputSource.writeOutput(event, context, TaskData.ofPropertyList(annotation.properties()));

    return future.get();
  }
}