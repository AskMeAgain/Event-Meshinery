package io.github.askmeagain.meshinery.connectors.kafka.factories;

import io.github.askmeagain.meshinery.connectors.kafka.MeshineryKafkaProperties;
import io.github.askmeagain.meshinery.core.other.MeshineryUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class KafkaConsumerFactory implements AutoCloseable {

  private final Map<List<String>, KafkaConsumer<String, byte[]>> consumers = new ConcurrentHashMap<>();
  private final Properties properties;
  private boolean disposed;

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

  public KafkaConsumer<String, byte[]> get(List<String> keys) {
    return consumers.computeIfAbsent(keys, this::createKafkaConsumer);
  }

  private KafkaConsumer<String, byte[]> createKafkaConsumer(List<String> topics) {
    log.info("Creating kafka consumer for topic '{}'", topics);
    var newProperties = new Properties();
    newProperties.putAll(properties);
    newProperties.setProperty("group.id", properties.getProperty("group.id") + "-" + topics);
    var stringKafkaConsumer = new KafkaConsumer<String, byte[]>(newProperties);

    stringKafkaConsumer.subscribe(topics);
    stringKafkaConsumer.poll(0);

    return stringKafkaConsumer;
  }

  @Override
  public void close() {
    if (disposed) {
      return;
    }
    consumers.forEach((k, v) -> v.close());
    disposed = true;
  }
}
