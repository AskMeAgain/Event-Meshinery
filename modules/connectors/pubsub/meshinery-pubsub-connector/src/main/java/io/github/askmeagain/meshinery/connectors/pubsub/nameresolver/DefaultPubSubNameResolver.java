package io.github.askmeagain.meshinery.connectors.pubsub.nameresolver;

public class DefaultPubSubNameResolver implements PubSubNameResolver {

  @Override
  public String resolveSubscriptionNameFromKey(String key) {
    return key + "_subscription";
  }
}
