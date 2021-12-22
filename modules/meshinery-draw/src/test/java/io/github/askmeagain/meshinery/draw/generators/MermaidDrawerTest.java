package io.github.askmeagain.meshinery.draw.generators;

import io.github.askmeagain.meshinery.draw.AbstractDrawerTestBase;
import io.github.askmeagain.meshinery.draw.MeshineryDrawer;
import io.github.askmeagain.meshinery.draw.customizer.EdgeCustomizer;
import io.github.askmeagain.meshinery.draw.customizer.GraphCustomizer;
import io.github.askmeagain.meshinery.draw.customizer.NodeCustomizer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class MermaidDrawerTest extends AbstractDrawerTestBase {

  @Test
  void test() {
    //Arrange --------------------------------------------------------------------------------
    var graphCustomizer = Mockito.mock(GraphCustomizer.class);
    var nodeCustomizer = Mockito.mock(NodeCustomizer.class);
    var edgeCustomizer = Mockito.mock(EdgeCustomizer.class);
    var tasks = getABCB();

    var drawer = new MeshineryDrawer(tasks);
    var graph = drawer.createGraph(graphCustomizer, nodeCustomizer, edgeCustomizer);

    //Act ------------------------------------------------------------------------------------
    var result = MermaidGenerator.createMermaidDiagram(graph);

    //Assert ---------------------------------------------------------------------------------
    assertThat(result).containsExactly("Doing_X --> Doing_Y", "Doing_Y --> Doing_Z", "Doing_Z --> Doing_Y");
  }
}
