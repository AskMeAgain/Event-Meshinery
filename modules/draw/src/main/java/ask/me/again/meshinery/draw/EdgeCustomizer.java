package ask.me.again.meshinery.draw;

import org.graphstream.graph.implementations.DefaultGraph;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface EdgeCustomizer {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  default void onEachEdge(DefaultGraph graph, Container container) {
    var edge = graph.addEdge(container.getId(), container.getFrom(), container.getTo(), true);
    edge.addAttribute("layout.weight", 5);
    edge.addAttribute("ui.style", """
        shape: line;
        size: 2px;
        """
    );
  }

}
