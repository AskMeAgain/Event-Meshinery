package io.github.askmeagain.meshinery.connectors.pubsub.nameresolver;

/**
 * Interface which resolves an input/output key to a pubsub subscription name
 */
@FunctionalInterface
public interface PubSubNameResolver {
  /**
   * Resolves a subscrion name from a given key
   *
   * @param key input/output event key
   * @return returns the subscription name of the given key
   */
  String resolveSubscriptionNameFromKey(String key);
}
