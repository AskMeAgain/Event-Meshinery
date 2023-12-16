package io.github.askmeagain.meshinery.connectors.pubsub;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannelBuilder;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractPubSubTestBase {

  private static final PubSubEmulatorContainer PUB_SUB_EMULATOR_CONTAINER = new PubSubEmulatorContainer(
      DockerImageName.parse("gcr.io/google.com/cloudsdktool/google-cloud-cli:457.0.0-emulators")
  );

  protected static final String TOPIC = "test-topic";

  @BeforeAll
  protected static void setup() {
    PUB_SUB_EMULATOR_CONTAINER.start();

    createTopic(TOPIC);
    createSubscription(TOPIC + "-subscription");
  }

  @SneakyThrows
  protected static void createSubscription(String subscription) {
    var subscriptionAdminSettings = SubscriptionAdminSettings.newBuilder()
        .setCredentialsProvider(getCredentialProvider())
        .setTransportChannelProvider(getTransportChannelProvider())
        .build();
    try (var subscriptionAdminClient = SubscriptionAdminClient.create(subscriptionAdminSettings)) {
      var subscriptionName = SubscriptionName.of(getProjectId(), subscription);
      try {
        subscriptionAdminClient.getSubscription(subscriptionName);
      } catch (NotFoundException e) {
        subscriptionAdminClient.createSubscription(
            subscriptionName,
            TopicName.of(getProjectId(), TOPIC),
            PushConfig.getDefaultInstance(),
            10
        );
      }
    }
  }

  @SneakyThrows
  protected static void createTopic(String topic) {
    var topicAdminSettings = TopicAdminSettings.newBuilder()
        .setTransportChannelProvider(getTransportChannelProvider())
        .setCredentialsProvider(getCredentialProvider())
        .build();
    try (var topicAdminClient = TopicAdminClient.create(topicAdminSettings)) {
      var topicName = TopicName.of(getProjectId(), topic);
      try {
        topicAdminClient.getTopic(topicName);
      } catch (NotFoundException e) {
        topicAdminClient.createTopic(topicName);
      }
    }
  }

  protected static NoCredentialsProvider getCredentialProvider() {
    return NoCredentialsProvider.create();
  }

  protected static String getEmulatorEndpoint() {
    return PUB_SUB_EMULATOR_CONTAINER.getEmulatorEndpoint();
  }

  protected static String getProjectId() {
    return "test-project";
  }

  protected static FixedTransportChannelProvider getTransportChannelProvider() {
    var channel = ManagedChannelBuilder.forTarget(getEmulatorEndpoint())
        .usePlaintext()
        .build();
    return FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
  }
}
