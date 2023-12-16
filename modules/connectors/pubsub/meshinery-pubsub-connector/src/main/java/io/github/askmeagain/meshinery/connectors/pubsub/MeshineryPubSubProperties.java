package io.github.askmeagain.meshinery.connectors.pubsub;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import lombok.Data;

@Data
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryPubSubProperties {

  @Positive
  private int limit = 10;

  @NotBlank
  private String projectId;

  private String emulatorEndpoint;
}
