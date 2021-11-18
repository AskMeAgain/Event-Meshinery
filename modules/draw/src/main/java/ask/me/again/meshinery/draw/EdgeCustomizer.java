package ask.me.again.meshinery.draw;

import org.graphstream.graph.implementations.DefaultGraph;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface EdgeCustomizer {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  default void onEachEdge(DefaultGraph graph, EdgeData edgeData) {
    var edge = graph.addEdge(edgeData.getId(), edgeData.getFrom(), edgeData.getTo(), true);
    edge.addAttribute("layout.weight", 5);
    edge.addAttribute("ui.style", """
        shape: line;
        size: 2px;
        """
    );
  }

}
