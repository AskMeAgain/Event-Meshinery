package ask.me.again.meshinery.draw;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.graphstream.graph.implementations.DefaultGraph;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface NodeCustomizer {

  Map<String, String> colors = new HashMap<>();
  Queue<String> possibleColors = new LinkedList<>(Set.of("blue", "red", "green"));

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  default void onEachNode(DefaultGraph graph, NodeData nodeData) {
    var node = graph.addNode(nodeData.getName());
    node.addAttribute("ui.label", nodeData.getName());
    node.addAttribute("layout.weight", 500);

    var source = nodeData.getTaskData().getSingle("graph.inputSource");

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
        stroke-mode: plain;
        stroke-color: white;
        stroke-width: 1px;
        text-alignment: at-right;
        """.formatted(color)
    );
  }
}
