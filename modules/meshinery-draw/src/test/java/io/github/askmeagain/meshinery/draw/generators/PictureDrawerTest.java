package io.github.askmeagain.meshinery.draw.generators;


import io.github.askmeagain.meshinery.draw.AbstractDrawerTestBase;
import io.github.askmeagain.meshinery.draw.MeshineryDrawProperties;
import io.github.askmeagain.meshinery.draw.MeshineryDrawer;
import io.github.askmeagain.meshinery.draw.customizer.EdgeCustomizer;
import io.github.askmeagain.meshinery.draw.customizer.GraphCustomizer;
import io.github.askmeagain.meshinery.draw.customizer.NodeCustomizer;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class PictureDrawerTest extends AbstractDrawerTestBase {

  @Test
  void testPictureGeneration() throws IOException {
    //Arrange --------------------------------------------------------------------------------
    var properties = new MeshineryDrawProperties();
    properties.setOutputFormat("PNG");
    properties.setResolution("HD720");

    var drawer = new MeshineryDrawer(getABCB());

    var edgeCustomizer = Mockito.spy(EdgeCustomizer.class);
    var nodeCustomizer = Mockito.spy(NodeCustomizer.class);
    var graphCustomizer = Mockito.spy(GraphCustomizer.class);

    //Act ------------------------------------------------------------------------------------
    var graph = drawer.createGraph(graphCustomizer, nodeCustomizer, edgeCustomizer);
    var image = PictureGenerator.createImage(properties, graph);

    //Assert ---------------------------------------------------------------------------------
    //Files.write(Path.of("./picture.png"), image);
    assertThat(image).isNotEmpty();
  }
}