package ask.me.again.meshinery.monitoring;

import ask.me.again.meshinery.spring.MeshineryAutoConfiguration;
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
@Import({MonitoringApiController.class, MeshineryMonitoringAutoConfiguration.class})
public @interface EnableMeshineryMonitoring {
}
