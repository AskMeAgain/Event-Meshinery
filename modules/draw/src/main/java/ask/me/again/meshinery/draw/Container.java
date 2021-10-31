package ask.me.again.meshinery.draw;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@SuppressWarnings("checkstyle:MissingJavadocType")
public class Container {
  String name;
  String id;
  String from;
  String to;
}
