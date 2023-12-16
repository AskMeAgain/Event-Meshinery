package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryConnector;
import java.io.IOException;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class PubSubConnector<C extends DataContext> implements MeshineryConnector<String, C>, AutoCloseable {

  @Getter
  private final String name;
  private final PubSubInputSource<C> pubsubInputSource;
  private final PubSubOutputSource<C> pubsubOutputSource;

  public PubSubConnector(
      Class<C> clazz,
      ObjectMapper objectMapper,
      MeshineryPubSubProperties pubSubProperties,
      TransportChannelProvider transportChannelProvider,
      CredentialsProvider credentialsProvider
  ) throws IOException {
    this(
        "default-pubsub-connector",
        clazz,
        objectMapper,
        pubSubProperties,
        transportChannelProvider,
        credentialsProvider
    );
  }

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public PubSubConnector(
      String name,
      Class<C> clazz,
      ObjectMapper objectMapper,
      MeshineryPubSubProperties pubSubProperties,
      TransportChannelProvider transportChannelProvider,
      CredentialsProvider credentialsProvider
  ) {
    this.name = name;
    this.pubsubInputSource = new PubSubInputSource<>(
        name,
        objectMapper,
        clazz,
        pubSubProperties,
        transportChannelProvider,
        credentialsProvider
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
  public void writeOutput(String key, C output) {
    pubsubOutputSource.writeOutput(key, output);
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