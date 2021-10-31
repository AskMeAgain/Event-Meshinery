package ask.me.again.meshinery.draw;

import org.graphstream.graph.implementations.DefaultGraph;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface GraphCustomizer {

  default void onGraph(DefaultGraph graph) {
  }

}
