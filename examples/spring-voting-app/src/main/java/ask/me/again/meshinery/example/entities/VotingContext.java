package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.Context;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Value
@Builder(toBuilder = true)
@Jacksonized
@AllArgsConstructor
public class VotingContext implements Context {

  String id;
  boolean approved;
}
