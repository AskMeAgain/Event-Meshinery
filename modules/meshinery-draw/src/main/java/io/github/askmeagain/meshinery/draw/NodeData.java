package io.github.askmeagain.meshinery.draw;

import io.github.askmeagain.meshinery.core.task.TaskData;
import lombok.AllArgsConstructor;
import lombok.Value;


@Value
@AllArgsConstructor
@SuppressWarnings("checkstyle:MissingJavadocType")
public class NodeData {
  String name;
  TaskData taskData;
}
