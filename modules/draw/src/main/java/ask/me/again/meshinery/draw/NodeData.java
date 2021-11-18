package ask.me.again.meshinery.draw;

import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class NodeData {
  String name;
  Properties properties;
}
