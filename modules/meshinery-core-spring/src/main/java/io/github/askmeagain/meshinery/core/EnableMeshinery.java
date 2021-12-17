package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.common.DataContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MeshineryAutoConfiguration.class)
public @interface EnableMeshinery {

  Class<? extends DataContext>[] injection() default {};

  Class<? extends DataContext>[] context() default {};

}
