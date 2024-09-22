package io.github.askmeagain.meshinery.aop.exception;

import java.lang.reflect.Method;

public class MeshineryAopWrongMethodParameterType extends RuntimeException {

  /**
   * Exception for multiple input parameters
   *
   * @param method
   */
  public MeshineryAopWrongMethodParameterType(Method method) {
    super("Method %s needs to contain only a single input parameter of type DataContext".formatted(method.getName()));
  }
}
