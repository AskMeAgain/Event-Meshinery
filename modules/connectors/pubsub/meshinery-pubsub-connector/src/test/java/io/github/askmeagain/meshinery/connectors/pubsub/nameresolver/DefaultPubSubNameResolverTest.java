package io.github.askmeagain.meshinery.connectors.pubsub.nameresolver;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPubSubNameResolverTest {

  @Test
  void resolveSubscriptionNameFromKey() {
    //Arrange --------------------------------------------------------------------------------
    var resolver = new DefaultPubSubNameResolver();

    //Act ------------------------------------------------------------------------------------
    var result = resolver.resolveSubscriptionNameFromKey("abc");

    //Assert ---------------------------------------------------------------------------------
    assertThat(result).isEqualTo("abc_subscription");
  }
}