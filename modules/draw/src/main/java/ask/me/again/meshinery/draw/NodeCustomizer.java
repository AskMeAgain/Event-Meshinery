package ask.me.again.meshinery.draw;

import org.graphstream.graph.implementations.DefaultGraph;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface NodeCustomizer {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  default void onEachNode(DefaultGraph graph, String nodeName) {
    var node = graph.addNode(nodeName);
    node.addAttribute("ui.label", nodeName);
    node.addAttribute("ui.style", "size: 2px; text-alignment: center;");
  }

}
