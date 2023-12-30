package io.github.askmeagain.meshinery.connectors.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import io.github.askmeagain.meshinery.connectors.pubsub.nameresolver.DefaultPubSubNameResolver;
import io.github.askmeagain.meshinery.connectors.pubsub.nameresolver.PubSubNameResolver;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
      ApplicationContext applicationContext, ObjectProvider<ObjectMapper> objectMapper,
      ObjectProvider<MeshineryPubSubProperties> meshineryPubSubProperties,
      ObjectProvider<MeshineryTransportChannelProvider> transportChannelProviders,
      ObjectProvider<CredentialsProvider> credentialsProviders,
      ObjectProvider<PubSubNameResolver> pubSubNameResolvers
  ) {
    return new DynamicPubSubConnectorRegistration(
        applicationContext, objectMapper,
        meshineryPubSubProperties,
        transportChannelProviders,
        credentialsProviders,
        pubSubNameResolvers
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
  @ConditionalOnMissingBean(CredentialsProvider.class)
  public CredentialsProvider defaultCloudCredentialsProvider() {
    return SubscriptionAdminSettings.defaultCredentialsProviderBuilder()
        .build();
  }

  @Bean
  @ConditionalOnMissingBean(TransportChannelProvider.class)
  public TransportChannelProvider defaultTransportChannelProvider() {
    return SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
        .setMaxInboundMessageSize(20 * 1024 * 1024) // 20MB (maximum message size).
        .build();
  }

  @Bean
  @ConditionalOnMissingBean(PubSubNameResolver.class)
  public PubSubNameResolver defaultPubSubNameResolver() {
    return new DefaultPubSubNameResolver();
  }

  @Bean
  public MeshineryTransportChannelProvider meshineryTransportChannelProvider(
      TransportChannelProvider transportChannelProvider
  ) {
    return new MeshineryTransportChannelProvider(transportChannelProvider);
  }
}
