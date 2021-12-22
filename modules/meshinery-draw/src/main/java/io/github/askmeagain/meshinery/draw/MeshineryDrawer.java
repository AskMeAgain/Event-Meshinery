package io.github.askmeagain.meshinery.draw;

import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.draw.customizer.EdgeCustomizer;
import io.github.askmeagain.meshinery.draw.customizer.GraphCustomizer;
import io.github.askmeagain.meshinery.draw.customizer.NodeCustomizer;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.graphstream.graph.implementations.DefaultGraph;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_OUTPUT_KEY;

@RequiredArgsConstructor
public class MeshineryDrawer {
  private final List<MeshineryTask<?, ?>> tasks;

  public DefaultGraph createGraph(
      GraphCustomizer graphCustomizer,
      NodeCustomizer nodeCustomizer,
      EdgeCustomizer edgeCustomizer
  ) {
    var graph = new DefaultGraph("id");

    var map = new HashMap<String, MeshineryTask<?, ?>>();

    //map setup
    tasks.forEach(task -> {
      task.getInputKeys().forEach(inputKey -> {
        map.put(inputKey.toString(), task);
      });
    });

    //all nodes
    tasks.forEach(task -> {
      var node = graph.addNode(task.getTaskName());
      nodeCustomizer.onEachNode(node, task.getTaskData());
    });

    //all edges
    tasks.forEach(task -> {
      task.getTaskData().get(GRAPH_OUTPUT_KEY)
          .forEach(outputKey -> {
            if (map.containsKey(outputKey)) {
              var otherTask = map.get(outputKey);
              var otherName = otherTask.getTaskName();
              var id = task.getTaskName() + "_" + otherName;
              var edge = graph.addEdge(id, task.getTaskName(), otherName, true);
              edgeCustomizer.onEachEdge(edge, outputKey, task.getTaskData(), otherTask.getTaskData());
            }
          });
    });

    graphCustomizer.onGraph(graph);

    return graph;
  }
}
