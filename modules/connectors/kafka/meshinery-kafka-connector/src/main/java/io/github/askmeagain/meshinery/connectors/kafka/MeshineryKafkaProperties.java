package io.github.askmeagain.meshinery.connectors.kafka;

import java.util.Properties;
import lombok.Data;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Data
public class MeshineryKafkaProperties {
  String bootstrapServers;
  String groupId;

  Properties producerProperties = new Properties();
  Properties consumerProperties = new Properties();
}
