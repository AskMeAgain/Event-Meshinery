package io.github.askmeagain.meshinery.connectors.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaConsumerFactory;
import io.github.askmeagain.meshinery.connectors.kafka.factories.KafkaProducerFactory;
import io.github.askmeagain.meshinery.connectors.kafka.source.KafkaInputSource;
import io.github.askmeagain.meshinery.connectors.kafka.source.KafkaOutputSource;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class KafkaConnector<C extends MeshineryDataContext>
    implements MeshinerySourceConnector<String, C>, AutoCloseable {

  @Getter
  private final String name;
  private final KafkaInputSource<C> inputSource;
  private final KafkaOutputSource<C> outputSource;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaConnector(Class<C> contextType, ObjectMapper objectMapper, MeshineryKafkaProperties properties) {
    this("kafka-default-connector", contextType, objectMapper, properties);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaConnector(String name, Class<C> clazz, ObjectMapper objectMapper, MeshineryKafkaProperties properties) {
    var kafkaConsumerFactory = new KafkaConsumerFactory(properties);
    var kafkaProducerFactory = new KafkaProducerFactory(properties);

    this.name = name;
    this.inputSource = new KafkaInputSource<>(name + "-input", clazz, objectMapper, kafkaConsumerFactory);
    this.outputSource = new KafkaOutputSource<>(name + "-output", kafkaProducerFactory, objectMapper);
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public KafkaConnector(
      String name,
      Class<C> clazz,
      ObjectMapper objectMapper,
      KafkaConsumerFactory consumerFactory,
      KafkaProducerFactory producerFactory
  ) {
    this.name = name;
    this.inputSource = new KafkaInputSource<>(name + "-input", clazz, objectMapper, consumerFactory);
    this.outputSource = new KafkaOutputSource<>(name + "-output", producerFactory, objectMapper);
  }

  @Override
  public List<C> getInputs(List<String> keys) {
    return inputSource.getInputs(keys);
  }

  @Override
  public void writeOutput(String key, C output, TaskData taskData) {
    this.outputSource.writeOutput(key, output, taskData);
  }

  @Override
  public void close() {
    inputSource.close();
    outputSource.close();
  }

  @Override
  public C commit(C context) {
    return inputSource.commit(context);
  }
}
