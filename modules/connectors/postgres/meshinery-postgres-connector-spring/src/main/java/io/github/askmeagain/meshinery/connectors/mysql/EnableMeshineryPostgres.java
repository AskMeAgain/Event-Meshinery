package io.github.askmeagain.meshinery.connectors.mysql;

import io.github.askmeagain.meshinery.core.MeshineryAutoConfiguration;
import io.github.askmeagain.meshinery.core.common.DataContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Import;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AutoConfigureBefore(MeshineryAutoConfiguration.class)
@Import({MeshineryPostgresConfiguration.class})
public @interface EnableMeshineryPostgres {

  @SuppressWarnings("checkstyle:MissingJavadocMethod") Class<? extends DataContext>[] context() default {};

}
