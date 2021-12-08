package io.github.askmeagain.meshinery.connectors.kafka.factories;

import io.github.askmeagain.meshinery.connectors.kafka.MeshineryKafkaProperties;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class KafkaConsumerFactory {

  private final Map<String, KafkaConsumer<String, byte[]>> consumers = new ConcurrentHashMap<>();
  private final Properties properties;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaConsumerFactory(MeshineryKafkaProperties meshineryKafkaProperties) {
    properties = new Properties();
    properties.setProperty("bootstrap.servers", meshineryKafkaProperties.getBootstrapServers());
    properties.setProperty("group.id", meshineryKafkaProperties.getGroupId());
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
