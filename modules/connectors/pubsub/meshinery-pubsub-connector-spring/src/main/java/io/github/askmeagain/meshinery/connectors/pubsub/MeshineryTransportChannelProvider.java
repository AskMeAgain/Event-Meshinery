package io.github.askmeagain.meshinery.connectors.pubsub;

import com.google.api.gax.rpc.TransportChannelProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MeshineryTransportChannelProvider implements AutoCloseable {

  private final TransportChannelProvider transportChannelProvider;

  @Override
  public void close() throws Exception {
    transportChannelProvider.getTransportChannel().close();
  }
}
