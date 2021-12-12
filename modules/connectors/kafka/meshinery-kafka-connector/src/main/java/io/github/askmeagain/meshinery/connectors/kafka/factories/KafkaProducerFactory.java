package io.github.askmeagain.meshinery.connectors.kafka.factories;

import io.github.askmeagain.meshinery.connectors.kafka.MeshineryKafkaProperties;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class KafkaProducerFactory implements AutoCloseable {

  private final KafkaProducer<String, byte[]> producer;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaProducerFactory(MeshineryKafkaProperties meshineryKafkaProperties) {
    var properties = new Properties();
    properties.putAll(meshineryKafkaProperties.getProducerProperties());
    properties.setProperty("bootstrap.servers", meshineryKafkaProperties.getBootstrapServers());
    properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
    producer = new KafkaProducer<>(properties);
  }

  public KafkaProducer<String, byte[]> get() {
    return producer;
  }

  @Override
  public void close() {
    producer.close();
  }
}
