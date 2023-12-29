package io.github.askmeagain.meshinery.connectors.pubsub.nameresolver;

public interface PubSubNameResolver {
  String resolveSubscriptionNameFromKey(String key);
}
