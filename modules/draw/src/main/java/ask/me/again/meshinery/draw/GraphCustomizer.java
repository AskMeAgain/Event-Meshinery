package ask.me.again.meshinery.draw;

import org.graphstream.graph.implementations.DefaultGraph;

public interface GraphCustomizer {

  default void onGraph(DefaultGraph graph) {
  }

}
