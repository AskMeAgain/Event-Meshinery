package io.github.askmeagain.meshinery.connectors.kafka.sources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.OutputSource;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;

@Slf4j
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

    //TODO flushing here??
    var record = new ProducerRecord<>(topic, key, value);
    log.info("Sending {}", topic);
    kafkaProducerFactory.get(topic).send(record).get();
  }
}
