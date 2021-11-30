package io.github.askmeagain.meshinery.connectors.kafka.sources;

import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.OutputSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;

@RequiredArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class KafkaOutputSource<C extends DataContext> implements OutputSource<String, C> {

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
