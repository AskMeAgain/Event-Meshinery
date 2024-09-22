package io.github.askmeagain.meshinery.aop.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MeshineryAopTask {

  String event() default "";

  String write() default "";

  String taskName() default "";

  String[] properties() default {};

  Class<Exception> retryOnException() default Exception.class;

  int retryCount() default 0;

  RetryType retryMethod() default RetryType.NONE;

}