package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import io.github.askmeagain.meshinery.connectors.pubsub.nameresolver.PubSubNameResolver;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshinerySourceConnector;
import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class PubSubConnector<C extends MeshineryDataContext>
    implements MeshinerySourceConnector<String, C>, AutoCloseable {

  @Getter
  private final String name;
  private final PubSubInputSource<C> pubsubInputSource;
  private final PubSubOutputSource<C> pubsubOutputSource;

  public PubSubConnector(
      Class<C> clazz,
      ObjectMapper objectMapper,
      MeshineryPubSubProperties pubSubProperties,
      TransportChannelProvider transportChannelProvider,
      CredentialsProvider credentialsProvider,
      PubSubNameResolver pubSubNameResolver
  ) {
    this(
        "default-pubsub-connector",
        clazz,
        objectMapper,
        pubSubProperties,
        transportChannelProvider,
        credentialsProvider,
        pubSubNameResolver
    );
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public PubSubConnector(
      String name,
      Class<C> clazz,
      ObjectMapper objectMapper,
      MeshineryPubSubProperties pubSubProperties,
      TransportChannelProvider transportChannelProvider,
      CredentialsProvider credentialsProvider,
      PubSubNameResolver pubSubNameResolver
  ) {
    this.name = name;
    this.pubsubInputSource = new PubSubInputSource<>(
        name,
        objectMapper,
        clazz,
        pubSubProperties,
        transportChannelProvider,
        credentialsProvider,
        pubSubNameResolver
    );
    this.pubsubOutputSource = new PubSubOutputSource<>(
        name,
        objectMapper,
        pubSubProperties,
        transportChannelProvider,
        credentialsProvider
    );
  }

  @Override
  public void writeOutput(String key, C output, TaskData taskData) {
    pubsubOutputSource.writeOutput(key, output, taskData);
  }

  @Override
  public List<C> getInputs(List<String> keys) {
    return pubsubInputSource.getInputs(keys);
  }

  @Override
  public void close() throws Exception {
    pubsubOutputSource.close();
  }
}