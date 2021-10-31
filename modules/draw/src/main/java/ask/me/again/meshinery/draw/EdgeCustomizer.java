package ask.me.again.meshinery.draw;

import org.graphstream.graph.implementations.DefaultGraph;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface EdgeCustomizer {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  default void onEachEdge(DefaultGraph graph, Container container) {
    var edge = graph.addEdge(container.getId(), container.getFrom(), container.getTo());
    edge.addAttribute("ui.label", container.getName());
    edge.addAttribute("ui.style", "size: 2px; text-alignment: center;");
  }

}
