package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Validated
@Configuration
@EnableConfigurationProperties
public class MeshineryPubSubConfiguration {

  @Bean
  public DynamicPubSubConnectorRegistration dynamicMysqlConnectorRegistration(
      ApplicationContext applicationContext,
      ObjectProvider<ObjectMapper> objectMapper,
      ObjectProvider<MeshineryPubSubProperties> meshineryPubSubProperties,
      ObjectProvider<TransportChannelProvider> transportChannelProviders,
      ObjectProvider<CredentialsProvider> credentialsProviders
  ) {
    return new DynamicPubSubConnectorRegistration(
        applicationContext,
        objectMapper,
        meshineryPubSubProperties,
        transportChannelProviders,
        credentialsProviders
    );
  }

  @Bean
  @ConditionalOnMissingBean(ObjectMapper.class)
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  @Validated
  @ConfigurationProperties("meshinery.connectors.pubsub")
  public MeshineryPubSubProperties pubsubProperties() {
    return new MeshineryPubSubProperties();
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "meshinery.connectors.pubsub",
      name = "emulatorEndpoint",
      havingValue = "NO_MATCH",
      matchIfMissing = true
  )
  @ConditionalOnMissingBean(TransportChannelProvider.class)
  public TransportChannelProvider transportChannelProvider() {
    return SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
        .setMaxInboundMessageSize(20 * 1024 * 1024) // 20MB (maximum message size).
        .build();
  }

  @Bean
  @ConditionalOnProperty(
      prefix = "meshinery.connectors.pubsub",
      name = "emulatorEndpoint",
      havingValue = "NO_MATCH",
      matchIfMissing = true
  )
  @ConditionalOnMissingBean(CredentialsProvider.class)
  public CredentialsProvider defaultCloudCredentialsProvider() {
    return SubscriptionAdminSettings.defaultCredentialsProviderBuilder()
        .build();
  }

  @Bean
  @ConditionalOnMissingBean(CredentialsProvider.class)
  @ConditionalOnProperty(prefix = "meshinery.connectors.pubsub", name = "emulatorEndpoint")
  public CredentialsProvider credentialsProvider() {
    return NoCredentialsProvider.create();
  }

  @Bean
  @ConditionalOnMissingBean(TransportChannelProvider.class)
  @ConditionalOnProperty(prefix = "meshinery.connectors.pubsub", name = "emulatorEndpoint")
  public TransportChannelProvider transportChannelProvider(MeshineryPubSubProperties pubSubProperties) {
    var channel = ManagedChannelBuilder.forTarget(pubSubProperties.getEmulatorEndpoint())
        .usePlaintext()
        .build();
    return FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
  }
}
