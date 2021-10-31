package ask.me.again.meshinery.draw;

import org.graphstream.graph.implementations.DefaultGraph;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface NodeCustomizer {

  default void onEachNode(DefaultGraph graph, String nodeName) {
    graph.addNode(nodeName);
  }

}
