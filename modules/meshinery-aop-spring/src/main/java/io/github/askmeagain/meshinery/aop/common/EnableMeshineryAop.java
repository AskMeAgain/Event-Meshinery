package io.github.askmeagain.meshinery.aop.common;

import io.github.askmeagain.meshinery.aop.MeshineryAopAutoConfiguration;
import io.github.askmeagain.meshinery.aop.RegistrarConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({MeshineryAopAutoConfiguration.class, RegistrarConfiguration.class})
public @interface EnableMeshineryAop {
}
