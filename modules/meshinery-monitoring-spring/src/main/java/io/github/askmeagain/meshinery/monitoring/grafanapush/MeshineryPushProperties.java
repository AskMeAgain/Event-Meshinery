package io.github.askmeagain.meshinery.monitoring.grafanapush;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@SuppressWarnings("checkstyle:MissingJavadocType")
@ConfigurationProperties("meshinery.monitoring.grafana-push")
public class MeshineryPushProperties {
  private boolean enabled;
  private String username;
  private String password;
  private String mermaidDiagramUrl;
  private String dataSourceUid;
  private String grafanaUrl;
  private String dashboardName = "SystemDiagram";
  private String metricQuery = "processing_counter{task_name='$taskName'}";
}