package io.github.askmeagain.meshinery.draw;

import lombok.Data;

@Data
public class DrawerProperties {
  private String outputFormat = "PNG";
  private String resolution = "HD720";
  private String grafanaCrossOrigin;
}
