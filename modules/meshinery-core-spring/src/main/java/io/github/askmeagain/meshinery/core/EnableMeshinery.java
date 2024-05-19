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

  /**
   * Creates an injection endpoint for this class
   */
  Class<? extends DataContext>[] injection() default {};


  /**
   * Takes a list of keyvalue entries and creates a memory connector on startup
   */
  KeyDataContext[] connector() default {};

  @interface KeyDataContext {
    /**
     * Context of the memory connector
     */
    Class<? extends DataContext> context();

    /**
     * Key type of the memory connector
     */
    Class<?> key();
  }
}
