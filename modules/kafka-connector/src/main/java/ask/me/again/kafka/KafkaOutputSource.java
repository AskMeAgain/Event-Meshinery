package ask.me.again.kafka;

import ask.me.again.core.common.Context;
import ask.me.again.core.common.OutputSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;

import java.util.Properties;

public class KafkaOutputSource<C extends Context> implements OutputSource<String, C> {

  private final KafkaProducer<String, byte[]> producer;
  private final ObjectMapper objectMapper;

  public KafkaOutputSource(ObjectMapper objectMapper) {
    Properties properties = new Properties();
    // Set the brokers (bootstrap servers)
    properties.setProperty("bootstrap.servers", "localhost:9092");
    // Set how to serialize key/value pairs
    properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    var serde = Serdes.ByteArray();

    properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

    this.objectMapper = objectMapper;
    producer = new KafkaProducer<>(properties);
  }

  @Override
  @SneakyThrows
  public void writeOutput(String topic, C output) {

    var key = output.getId();
    var value = objectMapper.writeValueAsBytes(output);

    ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, value);
    producer.send(record);
  }
}
