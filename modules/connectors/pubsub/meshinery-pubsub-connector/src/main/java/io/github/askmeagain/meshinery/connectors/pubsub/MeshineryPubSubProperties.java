package io.github.askmeagain.meshinery.connectors.pubsub;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryPubSubProperties {

  public static final String PUBSUB_ACK_METADATA_FIELD_NAME = "pubsub-ack-id";

  @Positive
  private int limit = 10;

  @NotBlank
  private String projectId;

  private String emulatorEndpoint;
}
