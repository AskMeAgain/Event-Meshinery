package ask.me.again.meshinery.draw;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MeshineryDrawerConfiguration.class)
public @interface EnableMeshineryDrawing {
}
