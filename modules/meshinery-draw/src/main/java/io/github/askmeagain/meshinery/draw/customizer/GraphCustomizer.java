package io.github.askmeagain.meshinery.draw.customizer;

import org.graphstream.graph.implementations.DefaultGraph;

@SuppressWarnings("checkstyle:MissingJavadocType")
public interface GraphCustomizer {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  default void onGraph(DefaultGraph graph) {
    graph.setAttribute("ui.quality");
    graph.setAttribute("layout.quality", 3);
    graph.setAttribute("ui.antialias");
    graph.setAttribute("ui.style", """
        fill-color: red;
        fill-mode: plain;
        """);
  }

}
