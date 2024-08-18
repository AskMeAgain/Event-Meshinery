package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PullRequest;
import io.github.askmeagain.meshinery.connectors.pubsub.nameresolver.PubSubNameResolver;
import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;
import io.github.askmeagain.meshinery.core.common.MeshineryInputSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static io.github.askmeagain.meshinery.connectors.pubsub.MeshineryPubSubProperties.PUBSUB_ACK_METADATA_FIELD_NAME;
import static io.github.askmeagain.meshinery.connectors.pubsub.MeshineryPubSubProperties.PUBSUB_EVENT_KEY_METADATA_FIELD_NAME;

@Slf4j
@SuppressWarnings("checkstyle:MissingJavadocType")
public class PubSubInputSource<C extends MeshineryDataContext> implements MeshineryInputSource<String, C> {

  @Getter
  private final String name;
  private final ObjectMapper objectMapper;
  private final Class<C> clazz;
  private final PubSubNameResolver pubSubNameResolver;
  private final MeshineryPubSubProperties meshineryPubSubProperties;
  private final SubscriberStubSettings subscriberStubSettings;
  private GrpcSubscriberStub subscriber;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @SneakyThrows
  public PubSubInputSource(
      String name,
      ObjectMapper objectMapper,
      Class<C> clazz,
      MeshineryPubSubProperties meshineryPubSubProperties,
      TransportChannelProvider transportChannelProvider,
      CredentialsProvider credentialsProvider,
      PubSubNameResolver pubSubNameResolver
  ) {
    this.meshineryPubSubProperties = meshineryPubSubProperties;
    this.name = name;
    this.objectMapper = objectMapper;
    this.clazz = clazz;
    this.pubSubNameResolver = pubSubNameResolver;
    this.subscriberStubSettings = SubscriberStubSettings.newBuilder()
        .setTransportChannelProvider(transportChannelProvider)
        .setCredentialsProvider(credentialsProvider)
        .build();
    this.subscriber = GrpcSubscriberStub.create(subscriberStubSettings);
  }

  private ConcurrentHashMap<String, GrpcSubscriberStub> map = new ConcurrentHashMap<>();

  @Override
  @SneakyThrows
  public List<C> getInputs(List<String> keys) {
    return keys.stream()
        .map(this::createDataRequest)
        .flatMap(Collection::stream)
        .toList();
  }

  private List<C> createDataRequest(String key) {
    var resolvedKey = pubSubNameResolver.resolveSubscriptionNameFromKey(key);
    var pullRequest = PullRequest.newBuilder()
        .setMaxMessages(meshineryPubSubProperties.getLimit())
        .setReturnImmediately(true)
        .setSubscription(ProjectSubscriptionName.format(meshineryPubSubProperties.getProjectId(), resolvedKey))
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
        var contextWithoutAckId = objectMapper.readValue(json, clazz);
        var pubSubContextTemp1 = contextWithoutAckId.setMetadata(PUBSUB_ACK_METADATA_FIELD_NAME, message.getAckId());
        var pubSubContext = pubSubContextTemp1.setMetadata(PUBSUB_EVENT_KEY_METADATA_FIELD_NAME, key);
        list.add(clazz.cast(pubSubContext));
        ackIds.add(message.getAckId());
      } catch (JsonProcessingException e) {
        //do nothing
        e.printStackTrace();
      }
    }

    if (meshineryPubSubProperties.getAckImmediatly()) {
      var acknowledgeRequest = AcknowledgeRequest.newBuilder()
          .setSubscription(ProjectSubscriptionName.format(meshineryPubSubProperties.getProjectId(), resolvedKey))
          .addAllAckIds(ackIds)
          .build();
      subscriber.acknowledgeCallable().call(acknowledgeRequest);
    }

    return list;
  }

  @Override
  public C commit(C context) {
    if (meshineryPubSubProperties.getAckImmediatly()) {
      return context;
    }

    var key = context.getMetadata(PUBSUB_EVENT_KEY_METADATA_FIELD_NAME);
    var resolvedKey = pubSubNameResolver.resolveSubscriptionNameFromKey(key);
    var ackId = context.getMetadata(PUBSUB_ACK_METADATA_FIELD_NAME);
    log.debug("Committing message with id {}", ackId);
    var acknowledgeRequest = AcknowledgeRequest.newBuilder()
        .setSubscription(ProjectSubscriptionName.format(meshineryPubSubProperties.getProjectId(), resolvedKey))
        .addAllAckIds(List.of(ackId))
        .build();

    subscriber.acknowledgeCallable().call(acknowledgeRequest);

    return context;
  }
}