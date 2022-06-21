package io.github.askmeagain.meshinery.connectors.kafka;

import java.util.Properties;
import javax.validation.constraints.NotBlank;
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
