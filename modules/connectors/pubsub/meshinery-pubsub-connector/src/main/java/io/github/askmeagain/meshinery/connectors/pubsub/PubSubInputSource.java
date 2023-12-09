package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.ReceivedMessage;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class PubSubInputSource<C extends DataContext> implements InputSource<String, C> {

  @Getter
  private final String name;
  private final ObjectMapper objectMapper;
  private final Class<C> clazz;
  private final MeshineryPubSubProperties meshineryPubSubProperties;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public PubSubInputSource(
      String name,
      ObjectMapper objectMapper,
      Class<C> clazz,
      MeshineryPubSubProperties meshineryPubSubProperties
  ) {
    this.meshineryPubSubProperties = meshineryPubSubProperties;
    this.name = name;
    this.objectMapper = objectMapper;
    this.clazz = clazz;
  }

  @Override
  @SneakyThrows
  public List<C> getInputs(List<String> keys) {
    var subscriberStubSettings = SubscriberStubSettings.newBuilder()
        .setTransportChannelProvider(SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
            .setMaxInboundMessageSize(20 * 1024 * 1024) // 20MB (maximum message size).
            .build())
        .build();

    try (SubscriberStub subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {
      return keys.stream()
          .map(x -> createDataRequest(subscriber, x))
          .flatMap(Collection::stream)
          .toList();
    }
  }

  private List<C> createDataRequest(SubscriberStub subscriber, String subscriptionId) {
    PullRequest pullRequest = PullRequest.newBuilder()
        .setMaxMessages(meshineryPubSubProperties.getLimit())
        .setSubscription(subscriptionId)
        .build();

    var pullResponse = subscriber.pullCallable().call(pullRequest);

    if (pullResponse.getReceivedMessagesList().isEmpty()) {
      return Collections.emptyList();
    }

    var ackIds = new ArrayList<String>();
    var list = new ArrayList<C>();

    for (ReceivedMessage message : pullResponse.getReceivedMessagesList()) {
      try {
        var json = message.getMessage().getData().toStringUtf8();
        list.add(objectMapper.readValue(json, clazz));
        ackIds.add(message.getAckId());
      } catch (JsonProcessingException e) {
        //do nothing
        e.printStackTrace();
      }
    }

    // Acknowledge received messages.
    var acknowledgeRequest = AcknowledgeRequest.newBuilder()
        .setSubscription(subscriptionId)
        .addAllAckIds(ackIds)
        .build();

    // Use acknowledgeCallable().futureCall to asynchronously perform this operation.
    subscriber.acknowledgeCallable().call(acknowledgeRequest);

    return list;
  }
}