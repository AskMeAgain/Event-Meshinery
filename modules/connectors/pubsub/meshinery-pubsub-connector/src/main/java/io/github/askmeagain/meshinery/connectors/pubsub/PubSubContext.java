package io.github.askmeagain.meshinery.connectors.pubsub;

import io.github.askmeagain.meshinery.core.common.DataContext;

public interface PubSubContext extends DataContext {

  String getAckId();

  PubSubContext withAckId(String ackId);
}
