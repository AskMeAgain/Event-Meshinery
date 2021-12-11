package io.github.askmeagain.meshinery.connectors.kafka.sources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.kafka.MeshineryKafkaProperties;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class KafkaConnector<C extends DataContext> implements MeshineryConnector<String, C>, AutoCloseable {

  @Getter
  private final String name;
  private final KafkaInputSource<C> inputSource;
  private final KafkaOutputSource<C> outputSource;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaConnector(
      Class<C> contextType, ObjectMapper objectMapper, MeshineryKafkaProperties meshineryKafkaProperties
  ) {
    this("kafka-default-connector", contextType, objectMapper, meshineryKafkaProperties);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaConnector(
      String name, Class<C> clazz, ObjectMapper objectMapper, MeshineryKafkaProperties meshineryKafkaProperties
  ) {
    var kafkaConsumerFactory = new KafkaConsumerFactory(meshineryKafkaProperties);
    var kafkaProducerFactory = new KafkaProducerFactory(meshineryKafkaProperties);

    this.name = name;
    this.inputSource = new KafkaInputSource<>(name + "-input", clazz, objectMapper, kafkaConsumerFactory);
    this.outputSource = new KafkaOutputSource<>(name + "-output", kafkaProducerFactory, objectMapper);
  }

  @Override
  public List<C> getInputs(String key) {
    return inputSource.getInputs(key);
  }

  @Override
  public void writeOutput(String key, C output) {
    this.outputSource.writeOutput(key, output);
  }

  @Override
  public void close() {
    inputSource.close();
    outputSource.close();
    log.info("CLOSING KAFKA STUFF --------------------------------------------------");
  }
}
