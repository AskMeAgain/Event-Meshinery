package io.github.askmeagain.meshinery.monitoring;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Data
public class MeshineryDrawProperties {

  @NotBlank
  private String outputFormat = "PNG";
  @NotBlank
  private String resolution = "HD720";

}
