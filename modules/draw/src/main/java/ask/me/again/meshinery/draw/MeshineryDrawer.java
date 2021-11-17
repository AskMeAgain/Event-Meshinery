package ask.me.again.meshinery.draw;

import ask.me.again.meshinery.core.task.MeshineryTask;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import lombok.Builder;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkImages;

@Builder
@SuppressWarnings("checkstyle:MissingJavadocType")
public class MeshineryDrawer {

  private final List<MeshineryTask<?, ?>> tasks;

  @Builder.Default
  private final FileSinkImages.OutputType outputType = FileSinkImages.OutputType.PNG;

  private final NodeCustomizer nodeAssignment;
  private final EdgeCustomizer edgeAssignment;
  private final GraphCustomizer graphAssignment;

  public byte[] draw(String subgraph) throws IOException {
    //var filteredTasks = tasks.stream().filter(x -> x.getGraphData())
    return draw(tasks);
  }

  public byte[] draw() throws IOException {
    return draw(tasks);
  }

  private byte[] draw(List<MeshineryTask<?, ?>> tasks) throws IOException {
    var graph = new DefaultGraph("id");
    var fileSinkImages = new FileSinkImages(outputType, FileSinkImages.Resolutions.HD720);

    fileSinkImages.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);

    var nodeSet = new HashSet<String>();
    var edges = new HashSet<Container>();

    for (var task : tasks) {
      var graphData = task.getGraphData();

      var inputKeys = graphData.get("graph.inputKey");
      for (var inputKey : inputKeys) {

        nodeSet.add(inputKey);

        if (inputKeys.size() > 1) {
          nodeSet.add("%s_%s_joined".formatted(inputKeys.get(0), inputKeys.get(1)));
        }

        for (var outputKeys : graphData.get("graph.outputKey")) {

          if (inputKeys.size() > 1) {
            //join
            var combinedNode = "%s_%s_joined".formatted(inputKeys.get(0), inputKeys.get(1));
            edges.add(Container.builder()
                .name(task.getTaskName())
                .id("%s_%s".formatted(inputKey, combinedNode))
                .from(inputKey)
                .to(combinedNode)
                .build());

            drawNormalEdge(nodeSet, edges, task, combinedNode, outputKeys);
          } else {
            drawNormalEdge(nodeSet, edges, task, inputKey, outputKeys);
          }
        }
      }
    }

    nodeSet.forEach(nodeName -> nodeAssignment.onEachNode(graph, nodeName));
    edges.forEach(container -> edgeAssignment.onEachEdge(graph, container));
    graphAssignment.onGraph(graph);

    var tempFile = Files.createTempFile("meshinary", ".jpg");
    System.setProperty(
        "gs.ui.renderer",
        "org.graphstream.ui.j2dviewer.J2DGraphRenderer"
    );
    fileSinkImages.setRenderer(FileSinkImages.RendererType.SCALA);
    fileSinkImages.writeAll(graph, tempFile.toString());
    return Files.readAllBytes(tempFile);
  }

  private void drawNormalEdge(
      HashSet<String> nodeSet, HashSet<Container> edges, MeshineryTask<?, ?> task, Object inputKey, Object outputKeys
  ) {
    edges.add(Container.builder()
        .name(task.getTaskName())
        .id("%s_%s".formatted(inputKey, outputKeys.toString()))
        .from(inputKey.toString())
        .to(outputKeys.toString())
        .build());
    nodeSet.add(outputKeys.toString());
  }

}
