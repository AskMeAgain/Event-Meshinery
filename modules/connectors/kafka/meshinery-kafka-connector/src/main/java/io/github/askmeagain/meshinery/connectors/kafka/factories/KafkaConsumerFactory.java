package io.github.askmeagain.meshinery.connectors.kafka.factories;

import io.github.askmeagain.meshinery.connectors.kafka.MeshineryKafkaProperties;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class KafkaConsumerFactory implements AutoCloseable {

  private final Map<String, KafkaConsumer<String, byte[]>> consumers = new ConcurrentHashMap<>();
  private final Properties properties;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaConsumerFactory(MeshineryKafkaProperties meshineryKafkaProperties) {
    properties = new Properties();
    properties.putAll(meshineryKafkaProperties.getConsumerProperties());
    properties.setProperty("bootstrap.servers", meshineryKafkaProperties.getBootstrapServers());
    properties.setProperty("group.id", meshineryKafkaProperties.getGroupId());
    properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
    properties.setProperty("auto.offset.reset", "earliest");
  }

  public KafkaConsumer<String, byte[]> get(String key) {
    return consumers.computeIfAbsent(key, this::createKafkaConsumer);
  }

  private KafkaConsumer<String, byte[]> createKafkaConsumer(String topic) {
    log.info("Creating kafka consumer for topic '{}'", topic);
    var stringKafkaConsumer = new KafkaConsumer<String, byte[]>(properties);

    stringKafkaConsumer.subscribe(List.of(topic));

    return stringKafkaConsumer;
  }

  @Override
  public void close() {
    consumers.forEach((k, v) -> v.close());
  }
}
