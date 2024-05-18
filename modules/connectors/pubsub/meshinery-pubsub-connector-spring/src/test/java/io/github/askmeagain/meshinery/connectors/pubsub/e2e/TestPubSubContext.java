package io.github.askmeagain.meshinery.connectors.pubsub.e2e;

import io.github.askmeagain.meshinery.connectors.pubsub.PubSubContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
public class TestPubSubContext implements PubSubContext {

  @With
  String id;
  @With
  int index;

  public TestPubSubContext(int index) {
    this.id = String.valueOf(index);
    this.index = index;
    this.ackId = null;
  }

  @With
  String ackId;
}
