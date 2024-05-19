package io.github.askmeagain.meshinery.connectors.kafka;

import jakarta.validation.constraints.NotBlank;
import java.util.Properties;
import lombok.Data;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Data
public class MeshineryKafkaProperties {

  @NotBlank
  String bootstrapServers;

  @NotBlank
  String groupId;

  Properties producerProperties = new Properties();

  Properties consumerProperties = new Properties();
}
