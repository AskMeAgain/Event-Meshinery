package io.github.askmeagain.meshinery.monitoring.generators;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.graphstream.graph.implementations.DefaultGraph;

@SuppressWarnings("checkstyle:MissingJavadocType")
@UtilityClass
public class MermaidGenerator {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public List<String> createMermaidDiagram(DefaultGraph graph) {

    var list = new ArrayList<String>();

    graph.getEachEdge().forEach(edge -> list.add(edge.getNode0().getId() + " --> " + edge.getNode1().getId()));

    return list;
  }
}
