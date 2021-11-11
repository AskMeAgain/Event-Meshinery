package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.Context;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class VoteContext implements Context {

  String id;
  boolean approved;
}
