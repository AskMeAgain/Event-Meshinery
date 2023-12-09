package io.github.askmeagain.meshinery.connectors.pubsub.factories;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.TopicName;
import io.github.askmeagain.meshinery.connectors.pubsub.MeshineryPubSubProperties;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.SneakyThrows;

public class PubSubProducerFactory implements AutoCloseable {

  @Getter
  private final Publisher publisher;

  @SneakyThrows
  public PubSubProducerFactory(MeshineryPubSubProperties meshineryPubSubProperties, String key) {
    TopicName topicName = TopicName.of(meshineryPubSubProperties.getProjectId(), key);

    publisher = Publisher.newBuilder(topicName).build();
  }

  @Override
  public void close() throws Exception {
    publisher.shutdown();
    publisher.awaitTermination(5, TimeUnit.SECONDS);
  }
}
