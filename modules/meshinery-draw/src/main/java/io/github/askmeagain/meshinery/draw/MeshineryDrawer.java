package io.github.askmeagain.meshinery.draw;

import io.github.askmeagain.meshinery.core.task.MeshineryTask;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkImages;

import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_INPUT_KEY;
import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_OUTPUT_KEY;
import static io.github.askmeagain.meshinery.core.task.TaskDataProperties.GRAPH_SUBGRAPH;

@Builder
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryDrawer {

  private final List<MeshineryTask<?, ?>> tasks;

  @NonNull
  private final DrawerProperties properties;

  private final NodeCustomizer nodeAssignment;
  private final EdgeCustomizer edgeAssignment;
  private final GraphCustomizer graphAssignment;

  public byte[] drawPng(String... subgraph) throws IOException {
    return drawPng(getTasksBySubgraph(subgraph));
  }

  private byte[] drawPng(List<MeshineryTask<?, ?>> tasks) throws IOException {

    var graph = new DefaultGraph("id");
    var fileSinkImages = new FileSinkImages(
        FileSinkImages.OutputType.valueOf(properties.getOutputFormat()),
        FileSinkImages.Resolutions.valueOf(properties.getResolution())
    );

    fileSinkImages.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);

    var nodeSet = new HashMap<String, NodeData>();
    var edges = new HashSet<EdgeData>();

    gatherGraphData(tasks, nodeSet, edges);

    nodeSet.forEach((k, nodeName) -> nodeAssignment.onEachNode(graph, nodeName));
    edges.forEach(container -> edgeAssignment.onEachEdge(graph, container));
    graphAssignment.onGraph(graph);

    var tempFile = Files.createTempFile("meshinery", ".png");
    System.setProperty(
        "gs.ui.renderer",
        "org.graphstream.ui.j2dviewer.J2DGraphRenderer"
    );
    fileSinkImages.setRenderer(FileSinkImages.RendererType.SCALA);
    fileSinkImages.writeAll(graph, tempFile.toString());
    return Files.readAllBytes(tempFile);
  }

  private List<MeshineryTask<?, ?>> getTasksBySubgraph(String... subgraph) {
    if (subgraph.length == 0) {
      return tasks;
    }

    var filteredTasks = new ArrayList<MeshineryTask<?, ?>>();
    for (var task : tasks) {
      var taskSubgraphs = task.getTaskData().get(GRAPH_SUBGRAPH);
      if (taskSubgraphs != null) {
        for (var providedSubgraph : subgraph) {
          if (taskSubgraphs.contains(providedSubgraph)) {
            filteredTasks.add(task);
          }
        }
      }
    }

    return filteredTasks;
  }

  public byte[] drawMermaidDiagram(String... subGraph) {
    return drawMermaidDiagram(getTasksBySubgraph(subGraph));
  }

  @SneakyThrows
  private byte[] drawMermaidDiagram(List<MeshineryTask<?, ?>> tasks) {

    var edges = new HashSet<EdgeData>();

    gatherGraphData(tasks, new HashMap<>(), edges);

    var tempFile = Files.createTempFile("mermaid", "txt");

    PrintWriter writer = new PrintWriter(tempFile.toAbsolutePath().toString(), StandardCharsets.UTF_8);
    writer.println("graph LR");

    edges.forEach(container -> {
      var str = removeStars(container.getFrom()) + " --> " + removeStars(container.getTo());
      writer.println(str);
    });

    writer.close();

    return Files.readAllBytes(tempFile.toAbsolutePath());
  }

  private void gatherGraphData(
      List<MeshineryTask<?, ?>> tasks, HashMap<String, NodeData> nodeSet, HashSet<EdgeData> edges
  ) {
    for (var task : tasks) {
      var taskData = task.getTaskData();

      var inputKeys = taskData.get(GRAPH_INPUT_KEY);
      for (var inputKey : inputKeys) {

        nodeSet.put(inputKey, new NodeData(inputKey, taskData));

        if (inputKeys.size() > 1) {
          var joinedName = "%s_%s_joined".formatted(inputKeys.get(0), inputKeys.get(1));
          nodeSet.put(joinedName, new NodeData(joinedName, taskData));
        }

        for (var outputKeys : taskData.get(GRAPH_OUTPUT_KEY)) {

          if (inputKeys.size() > 1) {
            //join
            var combinedNode = "%s_%s_joined".formatted(inputKeys.get(0), inputKeys.get(1));
            edges.add(
                new EdgeData(task.getTaskName(), "%s_%s".formatted(inputKey, combinedNode), inputKey, combinedNode));

            drawNormalEdge(nodeSet, edges, task, combinedNode, outputKeys);
          } else {
            drawNormalEdge(nodeSet, edges, task, inputKey, outputKeys);
          }
        }
      }
    }
  }

  private String removeStars(String container) {
    return container.replace('*', ' ');
  }

  private void drawNormalEdge(
      HashMap<String, NodeData> nodeSet, HashSet<EdgeData> edges, MeshineryTask<?, ?> task, String inputKey,
      String outputKeys
  ) {
    edges.add(new EdgeData(
        task.getTaskName(),
        "%s_%s".formatted(inputKey, outputKeys),
        inputKey,
        outputKeys
    ));
    nodeSet.put(outputKeys, new NodeData(outputKeys, task.getTaskData()));
  }

}
