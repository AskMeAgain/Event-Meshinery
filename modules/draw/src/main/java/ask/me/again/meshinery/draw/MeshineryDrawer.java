package ask.me.again.meshinery.draw;

import ask.me.again.meshinery.core.common.MeshineryTask;
import lombok.Builder;
import lombok.Value;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkImages;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;

public class MeshineryDrawer {

  public byte[] draw(List<MeshineryTask<?, ?>> tasks) throws IOException {
    var graph = new DefaultGraph("my beautiful graph");
    var fileSinkImages = new FileSinkImages(FileSinkImages.OutputType.PNG, FileSinkImages.Resolutions.VGA);

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

    nodeSet.forEach(graph::addNode);
    edges.forEach(container -> {
      var edge = graph.addEdge(container.id, container.from, container.to);
      edge.addAttribute("ui.label", container.getName());
      edge.addAttribute("ui.style", "size: 2px; text-alignment: center;");
    });

    var tempFile = Files.createTempFile("meshinary", ".jpg");

    fileSinkImages.writeAll(graph, tempFile.toString());
    return Files.readAllBytes(tempFile);
  }

  @Value
  @Builder
  private static class Container {
    String name;
    String id;
    String from;
    String to;
  }
}
