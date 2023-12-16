package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PullRequest;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import java.io.IOException;
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
  private final SubscriberStubSettings subscriberStubSettings;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public PubSubInputSource(
      String name,
      ObjectMapper objectMapper,
      Class<C> clazz,
      MeshineryPubSubProperties meshineryPubSubProperties,
      TransportChannelProvider transportChannelProvider,
      CredentialsProvider credentialsProvider
  ) throws IOException {
    this.meshineryPubSubProperties = meshineryPubSubProperties;
    this.name = name;
    this.objectMapper = objectMapper;
    this.clazz = clazz;
    this.subscriberStubSettings = SubscriberStubSettings.newBuilder()
        .setTransportChannelProvider(transportChannelProvider)
        .setCredentialsProvider(credentialsProvider)
        .build();
  }

  @Override
  @SneakyThrows
  public List<C> getInputs(List<String> keys) {
    try (var subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {
      return keys.stream()
          .map(subscriptionId -> createDataRequest(subscriber, subscriptionId))
          .flatMap(Collection::stream)
          .toList();
    }
  }

  private List<C> createDataRequest(SubscriberStub subscriber, String subscriptionId) {
    var pullRequest = PullRequest.newBuilder()
        .setMaxMessages(meshineryPubSubProperties.getLimit())
        .setSubscription(ProjectSubscriptionName.format(meshineryPubSubProperties.getProjectId(), subscriptionId))
        .build();

    var pullResponse = subscriber.pullCallable().call(pullRequest);

    if (pullResponse.getReceivedMessagesList().isEmpty()) {
      return Collections.emptyList();
    }

    var ackIds = new ArrayList<String>();
    var list = new ArrayList<C>();

    for (var message : pullResponse.getReceivedMessagesList()) {
      try {
        var json = message.getMessage().getData().toStringUtf8();
        list.add(objectMapper.readValue(json, clazz));
        ackIds.add(message.getAckId());
      } catch (JsonProcessingException e) {
        //do nothing
        e.printStackTrace();
      }
    }

    var acknowledgeRequest = AcknowledgeRequest.newBuilder()
        .setSubscription(ProjectSubscriptionName.format(meshineryPubSubProperties.getProjectId(), subscriptionId))
        .addAllAckIds(ackIds)
        .build();

    subscriber.acknowledgeCallable().call(acknowledgeRequest);

    return list;
  }
}