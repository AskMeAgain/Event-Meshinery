package ask.me.again.kafka;

import ask.me.again.core.common.Context;
import ask.me.again.core.common.InputSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KafkaInputSource<C extends Context> implements InputSource<String, C> {

  private final Map<String, KafkaConsumer<String, byte[]>> consumers = new HashMap<>();
  private final Class<C> serdeClazz;
  private final ObjectMapper objectMapper;
  private final Properties properties;

  public KafkaInputSource(Class<C> serdeClazz, ObjectMapper objectMapper) {
    this.serdeClazz = serdeClazz;
    this.objectMapper = objectMapper;
    properties = new Properties();
    properties.setProperty("bootstrap.servers", "localhost:9092");
    properties.setProperty("group.id", "try2");
    properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
    properties.setProperty("auto.offset.reset", "earliest");
  }

  @Override
  public List<C> getInputs(String key) {

    var result = consumers
      .computeIfAbsent(key, this::createKafkaConsumer)
      .poll(Duration.ofMillis(1000));

    return StreamSupport.stream(result.spliterator(), false)
      .map(ConsumerRecord::value)
      .map(x -> {
        try {
          return objectMapper.readValue(x, serdeClazz);
        } catch (IOException e) {
          e.printStackTrace();
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  private KafkaConsumer<String, byte[]> createKafkaConsumer(String topic) {
    var stringKafkaConsumer = new KafkaConsumer<String, byte[]>(properties);

    stringKafkaConsumer.subscribe(List.of(topic));

    return stringKafkaConsumer;
  }

}
