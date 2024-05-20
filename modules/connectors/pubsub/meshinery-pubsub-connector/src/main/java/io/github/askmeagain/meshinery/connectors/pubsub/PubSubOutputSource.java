package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.github.askmeagain.meshinery.connectors.pubsub.factories.PubSubProducerFactory;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.OutputSource;
import io.github.askmeagain.meshinery.core.task.TaskData;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class PubSubOutputSource<C extends DataContext> implements OutputSource<String, C>, AutoCloseable {

  @Getter
  private final String name;
  private final ObjectMapper objectMapper;
  private final PubSubProducerFactory pubSubProducerFactory;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public PubSubOutputSource(
      String name,
      ObjectMapper objectMapper,
      MeshineryPubSubProperties meshineryPubSubProperties,
      TransportChannelProvider transportChannelProvider,
      CredentialsProvider credentialsProvider
  ) {
    this.name = name;
    this.objectMapper = objectMapper;
    this.pubSubProducerFactory = new PubSubProducerFactory(
        meshineryPubSubProperties,
        transportChannelProvider,
        credentialsProvider
    );
  }

  @Override
  @SneakyThrows
  public void writeOutput(String key, C output, TaskData taskData) {
    var data = objectMapper.writeValueAsBytes(output);
    var pubsubMessage = PubsubMessage.newBuilder()
        .setData(ByteString.copyFrom(data))
        .build();

    pubSubProducerFactory.getOrCreate(key).publish(pubsubMessage).get();
  }

  @Override
  public void close() throws Exception {
    pubSubProducerFactory.close();
  }
}