package io.github.askmeagain.meshinery.draw.generators;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.graphstream.graph.implementations.DefaultGraph;

@UtilityClass
public class MermaidGenerator {

  public List<String> createMermaidDiagram(DefaultGraph graph) {

    var list = new ArrayList<String>();

    graph.getEachEdge().forEach(edge -> {
      list.add(edge.getNode0().getId() + " --> " + edge.getNode1().getId());
    });

    return list;
  }
}
