package io.github.askmeagain.meshinery.monitoring;

import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import io.github.askmeagain.meshinery.monitoring.customizer.EdgeCustomizer;
import io.github.askmeagain.meshinery.monitoring.customizer.GraphCustomizer;
import io.github.askmeagain.meshinery.monitoring.customizer.NodeCustomizer;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.graphstream.graph.implementations.DefaultGraph;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_INPUT_KEY;
import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_OUTPUT_KEY;

@SuppressWarnings("checkstyle:MissingJavadocType")
@RequiredArgsConstructor
public class MeshineryDrawer {
  private final List<MeshineryTask<?, ?>> tasks;

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  public DefaultGraph createGraph(
      GraphCustomizer graphCustomizer,
      NodeCustomizer nodeCustomizer,
      EdgeCustomizer edgeCustomizer
  ) {
    var graph = new DefaultGraph("id");

    var map = new HashMap<String, MeshineryTask<?, ?>>();

    //map setup
    tasks.forEach(task -> {
      task.getTaskData().get(GRAPH_INPUT_KEY)
          .forEach(inputKey -> {
            map.put(inputKey, task);
          });
    });

    //all nodes
    tasks.forEach(task -> {
      var node = graph.addNode(task.getTaskName());
      nodeCustomizer.onEachNode(node, task.getTaskData());
    });

    //all edges
    tasks.forEach(task -> {
      if (task.getTaskData() == null || !task.getTaskData().has(GRAPH_OUTPUT_KEY)) {
        return;
      }
      task.getTaskData().get(GRAPH_OUTPUT_KEY).forEach(outputKey -> {
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
