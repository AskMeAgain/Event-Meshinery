package io.github.askmeagain.meshinery.draw.customizer;

import io.github.askmeagain.meshinery.core.task.TaskData;
import org.graphstream.graph.Edge;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface EdgeCustomizer {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  default void onEachEdge(Edge edge, String edgeName, TaskData fromData, TaskData toData) {
    edge.addAttribute("layout.weight", 5);
    edge.addAttribute("ui.label", edgeName);
    edge.addAttribute("ui.style", """
        shape: line;
        text-size: 20px;
        arrow-size:10px;
        size: 2px;
        """
    );
  }

}
