package io.github.askmeagain.meshinery.draw.generators;

import io.github.askmeagain.meshinery.draw.MeshineryDrawProperties;
import java.nio.file.Files;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSinkImages;

@SuppressWarnings("checkstyle:MissingJavadocType")
@UtilityClass
public class PictureGenerator {

  @SuppressWarnings("checkstyle:MissingJavadocMethod")
  @SneakyThrows
  public static byte[] createImage(MeshineryDrawProperties properties, DefaultGraph graph) {
    var fileSinkImages = new FileSinkImages(
        FileSinkImages.OutputType.valueOf(properties.getOutputFormat()),
        FileSinkImages.Resolutions.valueOf(properties.getResolution())
    );

    fileSinkImages.setLayoutPolicy(FileSinkImages.LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
    fileSinkImages.setRenderer(FileSinkImages.RendererType.SCALA);

    var tempFile = Files.createTempFile("meshinery", ".png");
    System.setProperty(
        "gs.ui.renderer",
        "org.graphstream.ui.j2dviewer.J2DGraphRenderer"
    );

    fileSinkImages.writeAll(graph, tempFile.toString());
    return Files.readAllBytes(tempFile);
  }
}
