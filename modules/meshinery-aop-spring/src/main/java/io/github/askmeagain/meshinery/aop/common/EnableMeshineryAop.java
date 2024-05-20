package io.github.askmeagain.meshinery.aop.common;

import io.github.askmeagain.meshinery.aop.MeshineryAopAutoConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableAspectJAutoProxy
@Import(MeshineryAopAutoConfiguration.class)
public @interface EnableMeshineryAop {
}
