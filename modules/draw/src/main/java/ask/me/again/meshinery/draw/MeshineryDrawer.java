package ask.me.again.meshinery.draw;

import ask.me.again.meshinery.core.task.MeshineryTask;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import lombok.Builder;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkImages;

import static ask.me.again.meshinery.draw.DrawerProperties.GRAPH_INPUTKEY;
import static ask.me.again.meshinery.draw.DrawerProperties.GRAPH_OUTPUTKEY;
import static ask.me.again.meshinery.draw.DrawerProperties.GRAPH_SUBGRAPH;

@Builder
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryDrawer {

  private final List<MeshineryTask<?, ?>> tasks;

  @Builder.Default
  private final FileSinkImages.OutputType outputType = FileSinkImages.OutputType.PNG;

  private final NodeCustomizer nodeAssignment;
  private final EdgeCustomizer edgeAssignment;
  private final GraphCustomizer graphAssignment;

  public byte[] draw(String... subgraph) throws IOException {

    if (subgraph.length == 0) {
      return draw(tasks);
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

    return draw(filteredTasks);
  }

  private byte[] draw(List<MeshineryTask<?, ?>> tasks) throws IOException {
    var graph = new DefaultGraph("id");
    var fileSinkImages = new FileSinkImages(outputType, FileSinkImages.Resolutions.HD720);

    fileSinkImages.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);

    var nodeSet = new HashMap<String, NodeData>();
    var edges = new HashSet<EdgeData>();

    for (var task : tasks) {
      var graphData = task.getTaskData();

      var inputKeys = graphData.get(GRAPH_INPUTKEY);
      for (var inputKey : inputKeys) {

        nodeSet.put(inputKey, new NodeData(inputKey, graphData.getProperties()));

        if (inputKeys.size() > 1) {
          var joinedName = "%s_%s_joined".formatted(inputKeys.get(0), inputKeys.get(1));
          nodeSet.put(joinedName, new NodeData(joinedName, graphData.getProperties()));
        }

        for (var outputKeys : graphData.get(GRAPH_OUTPUTKEY)) {

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

  private void drawNormalEdge(
      HashMap<String, NodeData> nodeSet, HashSet<EdgeData> edges, MeshineryTask<?, ?> task, String inputKey,
      String outputKeys
  ) {
    edges.add(new EdgeData(
        task.getTaskName(),
        "%s_%s".formatted(inputKey, outputKeys.toString()),
        inputKey.toString(),
        outputKeys.toString()
    ));
    nodeSet.put(outputKeys.toString(), new NodeData(outputKeys.toString(), task.getTaskData().getProperties()));
  }

}
