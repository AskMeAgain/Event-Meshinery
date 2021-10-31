package ask.me.again.meshinery.connectors.kafka.properties;

import lombok.Data;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Data
public class KafkaProperties {
  String bootstrapServer;
  String groupId;
}
