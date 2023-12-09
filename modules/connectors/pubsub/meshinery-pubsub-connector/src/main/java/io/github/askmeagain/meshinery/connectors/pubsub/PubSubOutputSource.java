package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.github.askmeagain.meshinery.connectors.pubsub.factories.PubSubProducerFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.OutputSource;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class PubSubOutputSource<C extends DataContext> implements OutputSource<String, C> {

  @Getter
  private final String name;
  private final ObjectMapper objectMapper;
  private final MeshineryPubSubProperties meshineryPubSubProperties;
  private final ConcurrentHashMap<String, Publisher> concurrentHashMapStringPublisher;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public PubSubOutputSource(
      String name,
      ObjectMapper objectMapper,
      MeshineryPubSubProperties meshineryPubSubProperties
  ) {
    this.concurrentHashMapStringPublisher = new ConcurrentHashMap<>();
    this.name = name;
    this.meshineryPubSubProperties = meshineryPubSubProperties;
    this.objectMapper = objectMapper;
  }

  @Override
  @SneakyThrows
  public void writeOutput(String key, C output) {
    var publisher = concurrentHashMapStringPublisher.computeIfAbsent(
        key,
        k -> new PubSubProducerFactory(meshineryPubSubProperties, k).getPublisher()
    );

    var data = objectMapper.writeValueAsBytes(output);
    var pubsubMessage = PubsubMessage.newBuilder()
        .setData(ByteString.copyFrom(data))
        .build();

    publisher.publish(pubsubMessage);
  }
}

