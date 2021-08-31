package ask.me.again.meshinery.connectors.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaConsumerFactory {

  private final Map<String, KafkaConsumer<String, byte[]>> consumers = new HashMap<>();
  private final Properties properties;

  public KafkaConsumerFactory() {
    properties = new Properties();
    properties.setProperty("bootstrap.servers", "localhost:9092");
    properties.setProperty("group.id", "try2");
    properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
    properties.setProperty("auto.offset.reset", "earliest");
  }

  private KafkaConsumer<String, byte[]> createKafkaConsumer(String topic) {
    var stringKafkaConsumer = new KafkaConsumer<String, byte[]>(properties);

    stringKafkaConsumer.subscribe(List.of(topic));

    return stringKafkaConsumer;
  }

  public KafkaConsumer<String, byte[]> get(String key) {
    return consumers.computeIfAbsent(key, this::createKafkaConsumer);
  }

}
