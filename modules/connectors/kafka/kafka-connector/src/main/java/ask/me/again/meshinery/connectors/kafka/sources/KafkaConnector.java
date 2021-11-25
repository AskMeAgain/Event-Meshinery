package ask.me.again.meshinery.connectors.kafka.sources;

import ask.me.again.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import ask.me.again.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import ask.me.again.meshinery.connectors.kafka.properties.KafkaProperties;
import ask.me.again.meshinery.core.common.Context;
import ask.me.again.meshinery.core.common.InputSource;
import ask.me.again.meshinery.core.common.OutputSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class KafkaConnector<C extends Context> implements OutputSource<String, C>, InputSource<String, C> {

  @Getter
  private final String name;
  private final KafkaInputSource<C> inputSource;
  private final KafkaOutputSource<C> outputSource;


  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaConnector(String name, Class<C> clazz, ObjectMapper objectMapper, KafkaProperties kafkaProperties) {
    var kafkaConsumerFactory = new KafkaConsumerFactory(kafkaProperties);
    var kafkaProducerFactory = new KafkaProducerFactory(kafkaProperties);

    this.name = name;
    this.outputSource = new KafkaOutputSource<>(name, kafkaProducerFactory, objectMapper);
    this.inputSource = new KafkaInputSource<>(name, clazz, objectMapper, kafkaConsumerFactory);
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
