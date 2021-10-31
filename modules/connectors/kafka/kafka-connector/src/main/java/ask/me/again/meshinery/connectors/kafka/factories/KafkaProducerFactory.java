package ask.me.again.meshinery.connectors.kafka.factories;

import ask.me.again.meshinery.connectors.kafka.properties.KafkaProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;

public class KafkaProducerFactory {

  private final Map<String, KafkaProducer<String, byte[]>> producer = new HashMap<>();
  private final Properties properties;

  public KafkaProducerFactory(KafkaProperties kafkaProperties) {
    properties = new Properties();
    properties.setProperty("bootstrap.servers", kafkaProperties.getBootstrapServer());
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
