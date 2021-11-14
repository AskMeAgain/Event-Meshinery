package ask.me.again.meshinery.connectors.kafka.sources;

import ask.me.again.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.OutputSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;

@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class KafkaOutputSource<C extends Context> implements OutputSource<String, C> {

  @Getter
  private final String name;
  private final KafkaProducerFactory kafkaProducerFactory;
  private final ObjectMapper objectMapper;

  @Override
  @SneakyThrows
  public void writeOutput(String topic, C output) {

    var key = output.getId();
    var value = objectMapper.writeValueAsBytes(output);

    var record = new ProducerRecord<>(topic, key, value);
    kafkaProducerFactory.get(topic).send(record);
  }
}
