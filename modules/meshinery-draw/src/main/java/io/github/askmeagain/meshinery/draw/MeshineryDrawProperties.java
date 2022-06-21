package io.github.askmeagain.meshinery.draw;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MeshineryDrawProperties {
  @NotBlank
  private String outputFormat = "PNG";
  @NotBlank
  private String resolution = "HD720";

  private final DashboardPushProperties grafanaDashboardPush = new DashboardPushProperties();

  @Data
  public static class DashboardPushProperties {
    private boolean enabled;
    private String username;
    private String password;
    private String mermaidDiagramUrl;
    private String dataSourceUid;
    private String grafanaUrl;
    private String dashboardName = "SystemDiagram";
    private String metricQuery = "processing_counter{task_name='$taskName'}";
  }
}
