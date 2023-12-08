package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;
import com.google.pubsub.v1.SubscriptionName;
import io.github.askmeagain.meshinery.core.common.AccessingInputSource;
import io.github.askmeagain.meshinery.core.common.DataContext;
import io.github.askmeagain.meshinery.core.common.InputSource;
import io.github.askmeagain.meshinery.core.other.Blocking;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class PostgresInputSource<C extends DataContext> implements InputSource<String, C> {

  @Getter
  private final String name;
  private final ObjectMapper objectMapper;
  private final Class<C> clazz;
  private final String simpleName;
  private final MeshineryPubSubProperties meshineryPubSubProperties;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public PostgresInputSource(
      String name,
      ObjectMapper objectMapper,
      Class<C> clazz,
      MeshineryPubSubProperties meshineryPubSubProperties
  ) {
    this.meshineryPubSubProperties = meshineryPubSubProperties;
    this.name = name;
    this.objectMapper = objectMapper;
    this.clazz = clazz;
    this.simpleName = clazz.getSimpleName();
  }

  @Override
  @SneakyThrows
  public List<C> getInputs(List<String> keys) {
    var subscriberStubSettings = SubscriberStubSettings.newBuilder()
        .setTransportChannelProvider(
            SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
                .setMaxInboundMessageSize(20 * 1024 * 1024) // 20MB (maximum message size).
                .build())
        .build();

    try (SubscriberStub subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {
      String subscriptionName = ProjectSubscriptionName.format(projectId, subscriptionId);
      PullRequest pullRequest = PullRequest.newBuilder()
          .setMaxMessages(meshineryPubSubProperties.getLimit())
          .setSubscription(subscriptionName)
          .build();

      // Use pullCallable().futureCall to asynchronously perform this operation.
      PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);

      // Stop the program if the pull response is empty to avoid acknowledging
      // an empty list of ack IDs.
      if (pullResponse.getReceivedMessagesList().isEmpty()) {
        System.out.println("No message was pulled. Exiting.");
        return Collections.emptyList();
      }

      List<String> ackIds = new ArrayList<>();
      for (ReceivedMessage message : pullResponse.getReceivedMessagesList()) {
        var json = message.getMessage().getData().toStringUtf8();
        ackIds.add(message.getAckId());
      }

      // Acknowledge received messages.
      var acknowledgeRequest = AcknowledgeRequest.newBuilder()
          .setSubscription(subscriptionName)
          .addAllAckIds(ackIds)
          .build();

      // Use acknowledgeCallable().futureCall to asynchronously perform this operation.
      subscriber.acknowledgeCallable().call(acknowledgeRequest);

      return
    }
  }

