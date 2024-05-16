package io.github.askmeagain.meshinery.core;

import io.github.askmeagain.meshinery.core.common.DataContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ImportAutoConfiguration(MeshineryAutoConfiguration.class)
public @interface EnableMeshinery {

  @SuppressWarnings("checkstyle:MissingJavadocMethod") Class<? extends DataContext>[] injection() default {};

  @SuppressWarnings("checkstyle:MissingJavadocMethod") KeyDataContext[] connector() default {};

  @interface KeyDataContext {
    @SuppressWarnings("checkstyle:MissingJavadocMethod") Class<? extends DataContext> context();

    @SuppressWarnings("checkstyle:MissingJavadocMethod") Class<?> key();
  }
}
