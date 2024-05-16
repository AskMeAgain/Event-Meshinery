package io.github.askmeagain.meshinery.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class DynamicMeshineryReadJobAspect {

  @Before("@annotation(MeshineryReadTask)")
  public void logExecutionTime() {
    //we just do nothing
    log.info("From aspect");
  }
}