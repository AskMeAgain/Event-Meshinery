package io.github.askmeagain.meshinery.monitoring.customizer;

import io.github.askmeagain.meshinery.core.task.TaskData;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.graphstream.graph.Node;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_INPUT_SOURCE;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface NodeCustomizer {

  Map<String, String> colors = new HashMap<>();
  Queue<String> possibleColors = new LinkedList<>(Set.of("blue", "red", "green"));

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  default void onEachNode(Node node, TaskData nodeData) {
    node.addAttribute("layout.weight", 500);
    node.addAttribute("ui.label", node.getId());

    var source = nodeData.getSingle(GRAPH_INPUT_SOURCE);

    var color = "blue";

    if (colors.containsKey(source)) {
      color = colors.get(source);
    } else {
      var newColor = possibleColors.remove();
      colors.put(source, newColor);
      color = newColor;
    }


    node.addAttribute("ui.style", """
        shape: circle;
        size: 20px;
        fill-color: %s;
        text-size: 30px;
        stroke-mode: plain;
        stroke-color: white;
        stroke-width: 1px;
        text-alignment: at-right;
        """.formatted(color)
    );
  }
}
