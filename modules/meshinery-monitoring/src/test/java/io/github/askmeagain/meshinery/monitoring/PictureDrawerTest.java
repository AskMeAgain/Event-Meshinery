package io.github.askmeagain.meshinery.monitoring;


import io.github.askmeagain.meshinery.monitoring.customizer.EdgeCustomizer;
import io.github.askmeagain.meshinery.monitoring.customizer.GraphCustomizer;
import io.github.askmeagain.meshinery.monitoring.customizer.NodeCustomizer;
import io.github.askmeagain.meshinery.monitoring.generators.PictureGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class PictureDrawerTest extends AbstractDrawerTestBase {

  @Test
  void testPictureGeneration() {
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