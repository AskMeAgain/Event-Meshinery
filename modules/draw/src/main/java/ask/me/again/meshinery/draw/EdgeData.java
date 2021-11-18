package ask.me.again.meshinery.draw;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class EdgeData {
  String name;
  String id;
  String from;
  String to;
}
