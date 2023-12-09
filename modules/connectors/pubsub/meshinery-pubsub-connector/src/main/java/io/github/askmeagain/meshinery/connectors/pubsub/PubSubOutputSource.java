package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.OutputSource;
import java.util.concurrent.TimeUnit;
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

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public PubSubOutputSource(
      String name,
      ObjectMapper objectMapper,
      MeshineryPubSubProperties meshineryPubSubProperties
  ) {
    this.name = name;
    this.meshineryPubSubProperties = meshineryPubSubProperties;
    this.objectMapper = objectMapper;
  }

  @Override
  @SneakyThrows
  public void writeOutput(String key, C output) {
    TopicName topicName = TopicName.of(meshineryPubSubProperties.getProjectId(), key);

    Publisher publisher = null;
    try {
      // Create a publisher instance with default settings bound to the topic
      publisher = Publisher.newBuilder(topicName).build();

      var data = objectMapper.writeValueAsBytes(output);
      var pubsubMessage = PubsubMessage.newBuilder()
          .setData(ByteString.copyFrom(data))
          .build();

      // Once published, returns a server-assigned message id (unique within the topic)
      publisher.publish(pubsubMessage);
    } finally {
      if (publisher != null) {
        // When finished with the publisher, shutdown to free up resources.
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }
  }
}

