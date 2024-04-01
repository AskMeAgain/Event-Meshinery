package io.github.askmeagain.meshinery.connectors.pubsub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
public class PubSubTestContext implements PubSubContext {

  @With
  String id;
  @Getter
  @With
  String ackId;
  @With
  int index;

  public PubSubTestContext(int index) {
    this.id = String.valueOf(index);
    this.index = index;
    this.ackId = null;
  }
}
