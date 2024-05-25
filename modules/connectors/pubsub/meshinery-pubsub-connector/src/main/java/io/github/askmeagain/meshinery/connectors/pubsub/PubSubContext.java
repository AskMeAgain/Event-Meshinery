package io.github.askmeagain.meshinery.connectors.pubsub;

import io.github.askmeagain.meshinery.core.common.MeshineryDataContext;

public interface PubSubContext extends MeshineryDataContext {

  String getAckId();

  PubSubContext withAckId(String ackId);
}
