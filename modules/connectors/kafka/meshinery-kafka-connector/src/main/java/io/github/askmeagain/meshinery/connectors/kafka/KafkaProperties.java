package io.github.askmeagain.meshinery.connectors.kafka;

import lombok.Data;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Data
public class KafkaProperties {
  String bootstrapServer;
  String groupId;
}
