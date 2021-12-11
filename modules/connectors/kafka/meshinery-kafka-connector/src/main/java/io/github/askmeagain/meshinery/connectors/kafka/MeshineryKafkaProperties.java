package io.github.askmeagain.meshinery.connectors.kafka;

import java.util.Properties;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Data
@Validated
public class MeshineryKafkaProperties {
  @NotBlank
  String bootstrapServers;
  @NotBlank
  String groupId;

  Properties producerProperties;
  Properties consumerProperties;
}
