package ask.me.again.meshinery.connectors.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KafkaProducerFactory {

  private final Map<String, KafkaProducer<String, byte[]>> producer = new HashMap<>();
  private final Properties properties;

  public KafkaProducerFactory() {
    properties = new Properties();
    properties.setProperty("bootstrap.servers", "localhost:9092");
    properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
  }

  private KafkaProducer<String, byte[]> createKafkaProducer(String topic) {
    return new KafkaProducer<>(properties);
  }

  public KafkaProducer<String, byte[]> get(String key) {
    return producer.computeIfAbsent(key, this::createKafkaProducer);
  }

}