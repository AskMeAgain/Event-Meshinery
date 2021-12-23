package io.github.askmeagain.meshinery.draw;

import lombok.Data;

@Data
public class MeshineryDrawProperties {
  private String outputFormat = "PNG";
  private String resolution = "HD720";

  private final DashboardPushProperties grafanaDashboardPush = new DashboardPushProperties();

  @Data
  public static class DashboardPushProperties {
    private String username;
    private String password;
  }
}
