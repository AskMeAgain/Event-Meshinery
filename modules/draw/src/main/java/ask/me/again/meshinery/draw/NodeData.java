package ask.me.again.meshinery.draw;

import ask.me.again.meshinery.core.task.TaskData;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class NodeData {
  String name;
  TaskData taskData;
}
