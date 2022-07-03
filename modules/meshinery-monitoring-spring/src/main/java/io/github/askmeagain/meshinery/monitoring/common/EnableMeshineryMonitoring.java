package io.github.askmeagain.meshinery.monitoring.common;

import io.github.askmeagain.meshinery.core.MeshineryAutoConfiguration;
import io.github.askmeagain.meshinery.monitoring.apis.DrawerApiController;
import io.github.askmeagain.meshinery.monitoring.config.MeshineryDrawerConfiguration;
import io.github.askmeagain.meshinery.monitoring.config.MeshineryMonitoringAutoConfiguration;
import io.github.askmeagain.meshinery.monitoring.apis.MonitoringApiController;
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
@Import(MeshineryMonitoringAutoConfiguration.class)
public @interface EnableMeshineryMonitoring {
}
