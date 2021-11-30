package ask.me.again.meshinery.example.entities;

import io.github.askmeagain.meshinery.core.common.DataContext;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@SuppressWarnings("checkstyle:MissingJavadocType")
@Value
@Builder
@Jacksonized
public class VoteContext implements DataContext {

  String id;
  boolean approved;
}
