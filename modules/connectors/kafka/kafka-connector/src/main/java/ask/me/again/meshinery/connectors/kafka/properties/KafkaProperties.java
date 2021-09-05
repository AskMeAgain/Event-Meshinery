package ask.me.again.meshinery.connectors.kafka.properties;

import lombok.Data;

@Data
public class KafkaProperties {
  String bootstrapServer;
  String groupId;
}
