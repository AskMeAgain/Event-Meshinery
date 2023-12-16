package io.github.askmeagain.meshinery.connectors.pubsub.factories;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.TopicName;
import io.github.askmeagain.meshinery.connectors.pubsub.MeshineryPubSubProperties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class PubSubProducerFactory implements AutoCloseable {

  private final MeshineryPubSubProperties meshineryPubSubProperties;
  private final TransportChannelProvider transportChannelProvider;
  private final CredentialsProvider credentialsProvider;
  private final ConcurrentHashMap<String, Publisher> concurrentHashMapStringPublisher = new ConcurrentHashMap<>();

  @SneakyThrows
  public Publisher getOrCreate(String key) {
    return concurrentHashMapStringPublisher.computeIfAbsent(key, this::createProducer);
  }

  @SneakyThrows
  private Publisher createProducer(String key) {
    var topicName = TopicName.of(meshineryPubSubProperties.getProjectId(), key);

    return Publisher.newBuilder(topicName)
        .setChannelProvider(transportChannelProvider)
        .setCredentialsProvider(credentialsProvider)
        .build();
  }

  @Override
  public void close() throws InterruptedException {
    for (var entry : concurrentHashMapStringPublisher.entrySet()) {
      var publisher = entry.getValue();
      publisher.shutdown();
      publisher.awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
