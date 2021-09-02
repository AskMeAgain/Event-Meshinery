package ask.me.again.meshinery.draw;

import ask.me.again.meshinery.core.common.MeshineryTask;
import lombok.Builder;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkImages;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;

@Builder
public class MeshineryDrawer {

  private final List<MeshineryTask<?, ?>> tasks;

  @Builder.Default
  private final FileSinkImages.OutputType outputType = FileSinkImages.OutputType.PNG;

  private final ApplyNode nodeAssignment;
  private final ApplyEdge edgeAssignment;

  public byte[] draw() throws IOException {
    var graph = new DefaultGraph("id");
    var fileSinkImages = new FileSinkImages(outputType, FileSinkImages.Resolutions.VGA);

    fileSinkImages.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);

    var nodeSet = new HashSet<String>();
    var edges = new HashSet<Container>();

    for (var task : tasks) {
      nodeSet.add(task.getInputKey().toString());
      for (var outputKeys : task.getOutputKeys()) {
        edges.add(Container.builder()
          .name(task.getTaskName())
          .id(task.getInputKey() + "" + outputKeys.toString())
          .to(task.getInputKey().toString())
          .from(outputKeys.toString())
          .build());
        nodeSet.add(outputKeys.toString());
      }
    }

    nodeSet.forEach(nodeName -> nodeAssignment.onEachNode(graph, nodeName));
    edges.forEach(container -> edgeAssignment.onEachEdge(graph, container));

    var tempFile = Files.createTempFile("meshinary", ".jpg");

    fileSinkImages.writeAll(graph, tempFile.toString());
    return Files.readAllBytes(tempFile);
  }

}