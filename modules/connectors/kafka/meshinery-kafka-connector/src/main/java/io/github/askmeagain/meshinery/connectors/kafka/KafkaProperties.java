package io.github.askmeagain.meshinery.connectors.kafka;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Data
@Validated
public class KafkaProperties {
  @NotBlank
  String bootstrapServer;
  @NotBlank
  String groupId;
}
