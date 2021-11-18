package ask.me.again.meshinery.draw;

import org.graphstream.graph.implementations.DefaultGraph;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface NodeCustomizer {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  default void onEachNode(DefaultGraph graph, NodeData nodeData) {
    var node = graph.addNode(nodeData.getName());
    node.addAttribute("ui.label", nodeData.getName());
    node.addAttribute("layout.weight", 500);
    node.addAttribute("ui.style", """
        shape: circle;
        size: 20px;
        fill-color: black;
        stroke-mode: plain;
        stroke-color: white;
        stroke-width: 1px;
        text-alignment: at-right;
        """
    );
  }
}
