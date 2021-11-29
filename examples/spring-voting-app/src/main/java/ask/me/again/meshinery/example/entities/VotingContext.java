package ask.me.again.meshinery.example.entities;

import ask.me.again.meshinery.core.common.DataContext;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Value
@Builder(toBuilder = true)
@Jacksonized
@AllArgsConstructor
public class VotingContext implements DataContext {

  String id;
  boolean approved;
}
