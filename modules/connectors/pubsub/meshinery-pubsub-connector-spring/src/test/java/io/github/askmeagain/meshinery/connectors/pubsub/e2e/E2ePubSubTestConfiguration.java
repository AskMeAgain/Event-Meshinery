package io.github.askmeagain.meshinery.connectors.pubsub.e2e;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import io.github.askmeagain.meshinery.connectors.pubsub.EnableMeshineryPubSub;
import io.github.askmeagain.meshinery.connectors.pubsub.MeshineryPubSubProperties;
import io.github.askmeagain.meshinery.core.utils.context.TestContext;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
@EnableMeshineryPubSub(context = TestContext.class)
public class E2ePubSubTestConfiguration {

  @Bean
  @Primary
  public TransportChannelProvider transportChannelProvider(MeshineryPubSubProperties pubSubProperties) {
    var channel = ManagedChannelBuilder.forTarget(pubSubProperties.getEmulatorEndpoint())
        .usePlaintext()
        .build();
    return FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
  }

  @Bean
  @Primary
  public CredentialsProvider credentialsProvider() {
    return NoCredentialsProvider.create();
  }


}
