package io.github.askmeagain.meshinery.connectors.kafka.sources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.kafka.MeshineryKafkaProperties;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import io.github.askmeagain.meshinery.core.common.OutputSource;
import java.util.List;
import lombok.Getter;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class KafkaConnector<C extends DataContext> implements OutputSource<String, C>, InputSource<String, C> {

  @Getter
  private final String name;
  private final KafkaInputSource<C> inputSource;
  private final KafkaOutputSource<C> outputSource;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaConnector(
      Class<C> contextType, ObjectMapper objectMapper, MeshineryKafkaProperties meshineryKafkaProperties
  ) {
    this("default", contextType, objectMapper, meshineryKafkaProperties);
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
}
