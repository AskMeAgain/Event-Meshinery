package io.github.askmeagain.meshinery.connectors.kafka.factories;

import io.github.askmeagain.meshinery.connectors.kafka.MeshineryKafkaProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class KafkaProducerFactory {

  private final Map<String, KafkaProducer<String, byte[]>> producer = new HashMap<>();
  private final Properties properties;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaProducerFactory(MeshineryKafkaProperties meshineryKafkaProperties) {
    properties = new Properties();
    properties.setProperty("bootstrap.servers", meshineryKafkaProperties.getBootstrapServers());
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
