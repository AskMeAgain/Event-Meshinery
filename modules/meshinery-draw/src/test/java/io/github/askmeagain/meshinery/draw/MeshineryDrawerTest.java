package io.github.askmeagain.meshinery.draw;


import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

class MeshineryDrawerTest extends AbstractDrawerTestBase {

  @Test
  void testDrawerHooks() throws IOException {

    //Arrange --------------------------------------------------------------------------------
    var applyEdge = Mockito.mock(EdgeCustomizer.class);
    var applyNode = Mockito.mock(NodeCustomizer.class);
    var applyGraph = Mockito.mock(GraphCustomizer.class);

    var properties = new DrawerProperties();
    properties.setOutputFormat("PNG");
    properties.setResolution("HD720");

    var drawer = MeshineryDrawer.builder()
        .tasks(getABCB())
        .properties(properties)
        .graphAssignment(applyGraph)
        .nodeAssignment(applyNode)
        .edgeAssignment(applyEdge)
        .build();

    //Act ------------------------------------------------------------------------------------
    drawer.drawPng();

    //Assert ---------------------------------------------------------------------------------
    Mockito.verify(applyEdge, Mockito.times(3)).onEachEdge(any(), any());
    Mockito.verify(applyNode, Mockito.times(3)).onEachNode(any(), any());
    Mockito.verify(applyGraph, Mockito.times(1)).onGraph(any());
  }
}