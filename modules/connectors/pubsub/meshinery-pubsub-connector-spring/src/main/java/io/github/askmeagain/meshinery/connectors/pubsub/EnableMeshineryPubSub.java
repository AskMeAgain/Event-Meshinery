package io.github.askmeagain.meshinery.connectors.pubsub;

import io.github.askmeagain.meshinery.core.MeshineryAutoConfiguration;
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
@Import({MeshineryPubSubConfiguration.class})
public @interface EnableMeshineryPubSub {

  @SuppressWarnings("checkstyle:MissingJavadocMethod") Class<? extends PubSubContext>[] context() default {};

}
